package protocols.statemachine;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import protocols.agreement.PaxosInstance;
import protocols.agreement.messages.AcceptMessage;
import protocols.agreement.messages.NotifyMessage;
import protocols.agreement.messages.PrepareMessage;
import protocols.agreement.messages.RPCMessage;
import protocols.agreement.notifications.JoinedNotification;
import protocols.agreement.requests.AddReplicaRequest;
import protocols.agreement.requests.RemoveReplicaRequest;
import protocols.agreement.timers.TimerRPC;
import protocols.app.utils.Operation;

import pt.unl.fct.di.novasys.babel.core.GenericProtocol;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.channel.tcp.TCPChannel;
import pt.unl.fct.di.novasys.channel.tcp.events.*;
import pt.unl.fct.di.novasys.network.data.Host;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import protocols.agreement.Agreement;
import protocols.statemachine.notifications.ChannelReadyNotification;
import protocols.agreement.notifications.DecidedNotification;
import protocols.agreement.requests.ProposeRequest;
import protocols.statemachine.notifications.ExecuteNotification;
import protocols.statemachine.requests.OrderRequest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * This is NOT fully functional StateMachine implementation.
 * This is simply an example of things you can do, and can be used as a starting point.
 * <p>
 * You are free to change/delete anything in this class, including its fields.
 * The only thing that you cannot change are the notifications/requests between the StateMachine and the APPLICATION
 * You can change the requests/notification between the StateMachine and AGREEMENT protocol, however make sure it is
 * coherent with the specification shown in the project description.
 * <p>
 * Do not assume that any logic implemented here is correct, think for yourself!
 */
public class StateMachine extends GenericProtocol {
    private static final Logger logger = LogManager.getLogger(StateMachine.class);

    private enum State {JOINING, ACTIVE}

    private static final int ONGOING = 5;

    //Protocol information, to register in babel
    public static final String PROTOCOL_NAME = "StateMachine";
    public static final short PROTOCOL_ID = 200;

    private final Host self;     //My own address/port
    private final int channelId; //Id of the created channel

    private State state;
    private List<Host> membership;
    private Map<Integer, Operation> decided;
    private Map<Integer, Operation> mine_decided;


    private List<Operation> pending;
    private Map<Integer, Operation> deciding;
    private int nextInstance;
    private int lastDecided;
    private int waiting_decision;

    public StateMachine(Properties props) throws IOException, HandlerRegistrationException {
        super(PROTOCOL_NAME, PROTOCOL_ID);
        nextInstance = 0;
        lastDecided = -1;
        waiting_decision = 0;

        decided = new HashMap<Integer, Operation>();
        pending = new LinkedList<Operation>();
        deciding = new HashMap<Integer, Operation>();
        mine_decided = new HashMap<Integer, Operation>();

        String address = props.getProperty("address");
        String port = props.getProperty("p2p_port");

        logger.info("Listening on {}:{}", address, port);
        this.self = new Host(InetAddress.getByName(address), Integer.parseInt(port));

        Properties channelProps = new Properties();
        channelProps.setProperty(TCPChannel.ADDRESS_KEY, address);
        channelProps.setProperty(TCPChannel.PORT_KEY, port); //The port to bind to
        channelProps.setProperty(TCPChannel.HEARTBEAT_INTERVAL_KEY, "1000");
        channelProps.setProperty(TCPChannel.HEARTBEAT_TOLERANCE_KEY, "3000");
        channelProps.setProperty(TCPChannel.CONNECT_TIMEOUT_KEY, "1000");
        channelId = createChannel(TCPChannel.NAME, channelProps);

        /*-------------------- Register Channel Events ------------------------------- */
        registerChannelEventHandler(channelId, OutConnectionDown.EVENT_ID, this::uponOutConnectionDown);
        registerChannelEventHandler(channelId, OutConnectionFailed.EVENT_ID, this::uponOutConnectionFailed);
        registerChannelEventHandler(channelId, OutConnectionUp.EVENT_ID, this::uponOutConnectionUp);
        registerChannelEventHandler(channelId, InConnectionUp.EVENT_ID, this::uponInConnectionUp);
        registerChannelEventHandler(channelId, InConnectionDown.EVENT_ID, this::uponInConnectionDown);

        registerMessageHandler(channelId, AcceptMessage.MSG_ID, this::uponNotifyMessage);

        /*--------------------- Register Request Handlers ----------------------------- */
        registerRequestHandler(OrderRequest.REQUEST_ID, this::uponOrderRequest);

        /*--------------------- Register Notification Handlers ----------------------------- */
        subscribeNotification(DecidedNotification.NOTIFICATION_ID, this::uponDecidedNotification);

        /*--------------------- Register Timer Handlers ----------------------------- */
        registerTimerHandler(TimerRPC.TIMEOUT_ID, this::uponRPC);
    }

    @Override
    public void init(Properties props) {
        //Inform the state machine protocol about the channel we created in the constructor
        triggerNotification(new ChannelReadyNotification(channelId, self));

        String host = props.getProperty("initial_membership");
        String[] hosts = host.split(",");
        List<Host> initialMembership = new LinkedList<>();
        for (String s : hosts) {
            String[] hostElements = s.split(":");
            Host h;
            try {
                h = new Host(InetAddress.getByName(hostElements[0]), Integer.parseInt(hostElements[1]));
            } catch (UnknownHostException e) {
                throw new AssertionError("Error parsing initial_membership", e);
            }
            initialMembership.add(h);
        }

        //TODO ORDER MEMBERSHIP
        if (initialMembership.contains(self)) {
            state = State.ACTIVE;
            logger.info("Starting in ACTIVE as I am part of initial membership");
            //I'm part of the initial membership, so I'm assuming the system is bootstrapping
            membership = new LinkedList<>(initialMembership);

            Collections.sort(membership, new Comparator<Host>() {
                @Override
                public int compare(Host o1, Host o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });

            membership.forEach(this::openConnection);
            triggerNotification(new JoinedNotification(membership, 0));
        } else {
            state = State.JOINING;
            logger.info("Starting in JOINING as I am not part of initial membership");
            //You have to do something to join the system and know which instance you joined
            // (and copy the state of that instance)
        }
    }

    /*--------------------------------- Requests ---------------------------------------- */
    private void uponOrderRequest(OrderRequest request, short sourceProto) {
        logger.debug("Received request: " + request);
        Operation op = new Operation(Operation.NORMAL, request.getOpId().toString(), request.getOperation());
        if (state == State.JOINING) {
            //Do something smart (like buffering the requests)
            pending.add(op);
        } else if (state == State.ACTIVE) {
            //Also do something starter, we don't want an infinite number of instances active
            //Maybe you should modify what is it that you are proposing so that you remember that this
            //operation was issued by the application (and not an internal operation, check the uponDecidedNotification)
            //TODO if nextInstance - lastdecided > 5 aguardar
            pending.add(op);
            proposePending();

        }
    }

    /*--------------------------------- Notifications ---------------------------------------- */
    private void uponDecidedNotification(DecidedNotification notification, short sourceProto) {
        logger.debug("Received notification: " + notification);
        //Maybe we should make sure operations are executed in order?
        //You should be careful and check if this operation if an application operation (and send it up)
        //or if this is an operations that was executed by the state machine itself (in which case you should execute)
        if (notification.getInstance() >= nextInstance)
            nextInstance = notification.getInstance() + 1;
        Operation op = notification.getOperation();
        //decided.put(notification.getInstance(),new Operation(notification.getOperation(), notification.getOpId()));
        decided.put(notification.getInstance(), op);
        Operation proposed_op = deciding.remove(notification.getInstance());

        if (op.getOpType() != Operation.NORMAL) {
            processReplicaManagement(notification.getInstance(), op);
        }

        if (proposed_op != null) {
            if (proposed_op.equals(op)) {
                if (op.getOpType() == Operation.NORMAL) {
                    mine_decided.put(notification.getInstance(), op);
                }
            } else {
                pending.add(0, proposed_op);
            }
        }
        triggerExecute();
        proposePending();
    }

    private void processReplicaManagement(int instance, Operation operation) {
        ByteBuf buf = Unpooled.copiedBuffer(operation.getData());
        try {
            Host process = Host.serializer.deserialize(buf);

            if (operation.getOpType() == Operation.ADD) {
                AddReplicaRequest request = new AddReplicaRequest(instance, process);
                sendRequest(request, Agreement.PROTOCOL_ID);
                sendMessage(new NotifyMessage(instance, membership), process);
            } else if (operation.getOpType() == Operation.REMOVE) {
                RemoveReplicaRequest request = new RemoveReplicaRequest(instance, process);
                sendRequest(request, Agreement.PROTOCOL_ID);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uponNotifyMessage(NotifyMessage msg, Host host, short sourceProto, int channelId) {
        int instance = msg.getInstance();
        triggerNotification(new JoinedNotification(msg.getMembership(), instance));
    }

    private void triggerExecute() {
        while (decided.get(lastDecided + 1) != null) {
            Operation decideOp = decided.get(lastDecided + 1);
            Operation mine = mine_decided.get(lastDecided + 1);
            if (mine != null && mine.equals(decideOp)) {
                triggerNotification(new ExecuteNotification(UUID.fromString(decideOp.getKey()), decideOp.getData()));
            } else {
                if (mine == null) {
                    logger.debug(" proposed == null");
                } else {
                    logger.debug("Algo correu mal / mine=" + mine);
                }
            }
            lastDecided++;
            waiting_decision--;
        }
    }

    private void proposePending() {
        while (waiting_decision < ONGOING && !pending.isEmpty()) {
            Operation pending_operation = pending.remove(0);
            deciding.put(nextInstance, pending_operation);
            sendRequest(new ProposeRequest(nextInstance++, pending_operation.getKey().toString(), pending_operation), Agreement.PROTOCOL_ID);
            waiting_decision++;
        }
    }

    /*--------------------------------- Messages ---------------------------------------- */
    private void uponMsgFail(ProtoMessage msg, Host host, short destProto, Throwable throwable, int channelId) {
        //If a message fails to be sent, for whatever reason, log the message and the reason
        logger.error("Message {} to {} failed, reason: {}", msg, host, throwable);
    }

    /* --------------------------------- TCPChannel Events ---------------------------- */
    private void uponOutConnectionUp(OutConnectionUp event, int channelId) {
        logger.info("Connection to {} is up", event.getNode());
        ByteBuf buf = Unpooled.buffer();
        try {
            Host.serializer.serialize(event.getNode(), buf);
            Operation operation = new Operation(Operation.ADD, "ADD", buf.array());
            pending.add(0, operation);
            proposePending();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uponOutConnectionDown(OutConnectionDown event, int channelId) {
        logger.debug("Connection to {} is down, cause {}", event.getNode(), event.getCause());
        ByteBuf buf = Unpooled.buffer();
        try {
            Host.serializer.serialize(event.getNode(), buf);
            Operation operation = new Operation(Operation.REMOVE, "REMOVE", buf.array());
            pending.add(0, operation);
            proposePending();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void uponOutConnectionFailed(OutConnectionFailed<ProtoMessage> event, int channelId) {
        logger.debug("Connection to {} failed, cause: {}", event.getNode(), event.getCause());
        //Maybe we don't want to do this forever. At some point we assume he is no longer there.
        //Also, maybe wait a little bit before retrying, or else you'll be trying 1000s of times per second
        if (membership.contains(event.getNode()))
            openConnection(event.getNode());
    }

    private void uponInConnectionUp(InConnectionUp event, int channelId) {
        logger.trace("Connection from {} is up", event.getNode());
    }

    private void uponInConnectionDown(InConnectionDown event, int channelId) {
        logger.trace("Connection from {} is down, cause: {}", event.getNode(), event.getCause());
    }

    private void uponRPC(TimerRPC timer, long timerID) {
        RPCMessage message = new RPCMessage(lastDecided);
        for (Host host : membership) {
            sendMessage(message, host);
        }
    }

}
