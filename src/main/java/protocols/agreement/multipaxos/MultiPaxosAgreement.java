package protocols.agreement.multipaxos;

import protocols.agreement.PaxosInstance;
import protocols.agreement.multipaxos.messages.*;
import protocols.agreement.notifications.JoinedNotification;
import protocols.agreement.requests.*;
import protocols.agreement.timers.Timeout;
import protocols.app.utils.Tuple;
import pt.unl.fct.di.novasys.babel.core.GenericProtocol;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.data.Host;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import protocols.statemachine.notifications.ChannelReadyNotification;
import protocols.agreement.notifications.DecidedNotification;

import java.io.IOException;
import java.util.*;

public class MultiPaxosAgreement extends GenericProtocol {

    private static final Logger logger = LogManager.getLogger(MultiPaxosAgreement.class);

    //Protocol information, to register in babel
    public final static short PROTOCOL_ID = 200;
    public final static String PROTOCOL_NAME = "MultiPaxosAgreement";

    private Host myself;
    private int joinedInstance;
    private List<Host> membership;

    private HashMap<Integer, PaxosInstance> paxosInstancesMap;
    //no timeout for instances with no initial value to propose
    private HashMap<Long, Integer> timeoutInstancesMap;

    private final int agreementTime;
    int decider;

    public MultiPaxosAgreement(Properties props) throws IOException, HandlerRegistrationException {
        super(PROTOCOL_NAME, PROTOCOL_ID);
        joinedInstance = -1; //-1 means we have not yet joined the system
        membership = null;
        paxosInstancesMap = new HashMap<>();
        timeoutInstancesMap = new HashMap<>();

        decider=0;

        this.agreementTime = Integer.parseInt(props.getProperty("agreement_time", "10000"));

        /*--------------------- Register Timer Handlers ----------------------------- */
        registerTimerHandler(Timeout.TIMEOUT_ID, this::uponTimeout);

        /*--------------------- Register Request Handlers ----------------------------- */
        registerRequestHandler(ProposeRequest.REQUEST_ID, this::uponProposeRequest);
        registerRequestHandler(AddReplicaRequest.REQUEST_ID, this::uponAddReplica);
        registerRequestHandler(RemoveReplicaRequest.REQUEST_ID, this::uponRemoveReplica);

        /*--------------------- Register Notification Handlers ----------------------------- */
        subscribeNotification(ChannelReadyNotification.NOTIFICATION_ID, this::uponChannelCreated);
        subscribeNotification(JoinedNotification.NOTIFICATION_ID, this::uponJoinedNotification);
    }

    @Override
    public void init(Properties props) {
        //Nothing to do here, we just wait for events from the application or agreement
    }

    //Upon receiving the channelId from the membership, register our own callbacks and serializers
    private void uponChannelCreated(ChannelReadyNotification notification, short sourceProto) {
        int cId = notification.getChannelId();
        myself = notification.getMyself();
        logger.info("Channel {} created, I am {}", cId, myself);
        // Allows this protocol to receive events from this channel.
        registerSharedChannel(cId);
        /*---------------------- Register Message Serializers ---------------------- */
        registerMessageSerializer(cId, MPBroadcastMessage.MSG_ID, MPBroadcastMessage.serializer);
        registerMessageSerializer(cId, MPAcceptMessage.MSG_ID, MPAcceptMessage.serializer);
        registerMessageSerializer(cId, MPAcceptOkMessage.MSG_ID, MPAcceptOkMessage.serializer);
        registerMessageSerializer(cId, MPNotifyMessage.MSG_ID, MPNotifyMessage.serializer);
        registerMessageSerializer(cId, MPPrepareMessage.MSG_ID, MPPrepareMessage.serializer);
        registerMessageSerializer(cId, MPPrepareOkMessage.MSG_ID, MPPrepareOkMessage.serializer);
        /*---------------------- Register Message Handlers -------------------------- */
        try {
            //registerMessageHandler(cId, BroadcastMessage.MSG_ID, this::uponBroadcastMessage, this::uponMsgFail);
            registerMessageHandler(cId, MPPrepareMessage.MSG_ID, this::uponPrepareMessage);
            registerMessageHandler(cId, MPPrepareOkMessage.MSG_ID, this::uponPrepareOkMessage);
            registerMessageHandler(cId, MPAcceptMessage.MSG_ID, this::uponAcceptMessage);
            registerMessageHandler(cId, MPAcceptOkMessage.MSG_ID, this::uponAcceptOkMessage);
        } catch (HandlerRegistrationException e) {
            throw new AssertionError("Error registering message handler.", e);
        }
    }

    private void uponBroadcastMessage(MPBroadcastMessage msg, Host host, short sourceProto, int channelId) {
        if (joinedInstance >= 0) {
            //Obviously your agreement protocols will not decide things as soon as you receive the first message
            triggerNotification(new DecidedNotification(msg.getInstance(), msg.getOp().getLastOperation()));
        } else {
            //We have not yet received a JoinedNotification, but we are already receiving messages from the other
            //agreement instances, maybe we should do something with them...?
        }
    }

    private void uponJoinedNotification(JoinedNotification notification, short sourceProto) {
        //We joined the system and can now start doing things
        if (joinedInstance < 0 || joinedInstance != notification.getJoinInstance()){
            joinedInstance = notification.getJoinInstance();
            membership = new LinkedList<>(notification.getMembership());
            logger.info("Agreement starting at instance {},  membership: {}", joinedInstance, membership);
        }

        /*System.out.println(myself.getPort());
        Operation op = new Operation(Operation.NORMAL, String.valueOf(myself.getPort()), "Teste".getBytes(StandardCharsets.UTF_8));
        ProposeRequest pr = new ProposeRequest(0, "TESTE", op);
        uponProposeRequest(pr, Agreement.PROTOCOL_ID);
        System.out.println("Teste");*/
    }

    private int buildSeqNum(Host[] membership) {
        for (int selfID = 0; selfID < membership.length; selfID++)
            if (membership[selfID].equals(myself))
                return selfID;
        return -1;
    }

    private void uponProposeRequest(ProposeRequest request, short sourceProto) {
        logger.debug("Received " + request);
        if (joinedInstance <= request.getInstance() && joinedInstance >= 0) {
            int instanceID = request.getInstance();
            PaxosInstance instance = paxosInstancesMap.get(instanceID);
            int selfID = buildSeqNum(membership.toArray(new Host[membership.size()]));
            if (instance == null) {
                //create instance in map
                instance = new PaxosInstance(request.getOperation(), selfID+(membership.size()*request.getHandicap()), membership);
                paxosInstancesMap.put(instanceID, instance);
            } else {
                instance.setProposer_seq(selfID+(membership.size()*request.getHandicap()));
                instance.setProposer_value(request.getOperation());
                instance.setPrepare_ok_set(new LinkedList<Tuple>());
            }

            //broadcast prepare for all replicas
            MPPrepareMessage MPPrepareMessage = new MPPrepareMessage(instanceID, instance.getProposer_seq());
            for (Host host : instance.getAll_processes()) {
                sendMessage(MPPrepareMessage, host);
            }
            createTimeout(instanceID);
        }
    }

    private void uponPrepareMessage(MPPrepareMessage msg, Host host, short sourceProto, int channelId) {
        logger.debug("Prepare from {} " + msg.toString(), host.toString());
        PaxosInstance instance = paxosInstancesMap.get(msg.getInstance());
        if (joinedInstance <= msg.getInstance() && joinedInstance >= 0) {
            if (instance == null) {
                if (membership.contains(host)) {
                    int selfID = buildSeqNum(membership.toArray(new Host[membership.size()]));

                    instance = new PaxosInstance(null, selfID, membership);
                    instance.setHighest_prepare(msg.getProposer_seq());
                    paxosInstancesMap.put(msg.getInstance(), instance);
                    MPPrepareOkMessage message = new MPPrepareOkMessage(
                            msg.getInstance(),
                            msg.getProposer_seq(),
                            -1,
                            null);
                    sendMessage(message, host);
                }
            } else {
                if (instance.getAll_processes().contains(host)) {
                    if (msg.getProposer_seq() > instance.getHighest_prepare()) {
                        instance.setHighest_prepare(msg.getProposer_seq());
                        MPPrepareOkMessage message = new MPPrepareOkMessage(
                                msg.getInstance(),
                                msg.getProposer_seq(),
                                instance.getHighest_accepted(),
                                new OperationLog(instance.getHighest_value()));
                        sendMessage(message, host);
                    }
                }
            }
        }
    }

    //searches for the highest seq_number and returns the corresponding tuple
    private Tuple highest(List<Tuple> set) {
        Tuple res = null;
        for (Tuple pair : set) {
            if (res == null) {
                res = pair;
            } else {
                if (pair.getSeq() > res.getSeq())
                    res = pair;
            }
        }
        return res;
    }

    //Nao deverá ser nula a instacia pois ja fez prepare
    private void uponPrepareOkMessage(MPPrepareOkMessage msg, Host host, short sourceProto, int channelId) {
        logger.debug("PrepareOk {} " + msg.toString());
        PaxosInstance instance = paxosInstancesMap.get(msg.getInstance());
        if (joinedInstance <= msg.getInstance() && joinedInstance >= 0) {
            if (instance.getAll_processes().contains(host)) {
                if (msg.getProposer_seq() == instance.getProposer_seq()) {
                    instance.add_prepare_ok(msg.getHighest_seq(), msg.getHighest_val().getLastOperation());
                    if (instance.getPrepare_ok_set().size() >= (instance.getAll_processes().size() / 2) + 1) {
                        Tuple highest = highest(instance.getPrepare_ok_set());
                        if (highest.getVal() != null) {
                            instance.setProposer_value(highest.getVal());
                        }
                        MPAcceptMessage acceptMessage = new MPAcceptMessage(msg.getInstance(),
                                instance.getProposer_seq(),
                                new OperationLog(instance.getProposer_value()));
                        for (Host h : instance.getAll_processes()) {
                            sendMessage(acceptMessage, h);
                        }
                        instance.setPrepare_ok_set(new LinkedList<Tuple>());
                        createTimeout(msg.getInstance());
                    }
                }
            }
        }
    }

    private void createTimeout(int instance) {
        //setupPeriodicTimer(timeOut, this.fixTime, this.fixTime);
        cancelTimeout(instance);
        long timerId = setupTimer(new Timeout(), this.agreementTime);
        PaxosInstance paxos = paxosInstancesMap.get(instance);
        paxos.setTimerId(timerId);
        timeoutInstancesMap.put(timerId, instance);
    }

    private void cancelTimeout(int instance) {
        PaxosInstance paxos = paxosInstancesMap.get(instance);
        cancelTimer(paxos.getTimerId());
        timeoutInstancesMap.remove(paxos.getTimerId());
    }

    private void uponAcceptMessage(MPAcceptMessage msg, Host host, short sourceProto, int channelId) {
        logger.debug("Accept {} " + msg.toString());
        PaxosInstance instance = paxosInstancesMap.get(msg.getInstance());
        if (joinedInstance <= msg.getInstance() && joinedInstance >= 0) {
            if (instance == null) {
                int selfID = buildSeqNum(membership.toArray(new Host[membership.size()]));
                instance = new PaxosInstance(msg.getValue().getLastOperation(), selfID, membership);
            }
            if (instance.getAll_processes().contains(host)) {
                if (msg.getProposer_seq() >= instance.getHighest_prepare()) {
                    instance.setHighest_prepare(msg.getProposer_seq());
                    instance.setHighest_accepted(msg.getProposer_seq());
                    instance.setHighest_value(msg.getValue().getLastOperation());
                    MPAcceptOkMessage message = new MPAcceptOkMessage(
                            msg.getInstance(),
                            msg.getProposer_seq(),
                            msg.getValue());
                    for (Host h : instance.getAll_processes()) {
                        sendMessage(message, h);
                    }
                }
            }
        }
    }

    private boolean verifyIfAllEq(List<Tuple> accept_set, Tuple pair) {
        for (Tuple tup : accept_set) {
            if (!pair.getVal().equals(tup.getVal()))
                return false;
            if (pair.getSeq() != tup.getSeq())
                return false;
        }
        return true;
    }

    private boolean verifyIfBigger(List<Tuple> accept_set, Tuple pair) {
        for (Tuple tup : accept_set) {
            if (tup.getSeq() < pair.getSeq())
                return true;
        }
        return false;
    }

    private void uponAcceptOkMessage(MPAcceptOkMessage msg, Host host, short sourceProto, int channelId) {
        logger.debug("AcceptOk " + msg.toString());
        PaxosInstance instance = paxosInstancesMap.get(msg.getInstance());
        if (joinedInstance <= msg.getInstance() && joinedInstance >= 0) {
            if (instance == null) {
                int selfID = buildSeqNum(membership.toArray(new Host[membership.size()]));
                instance = new PaxosInstance(msg.getValue().getLastOperation(), selfID, membership);
            }

            if (instance.getAll_processes().contains(host)) {
                Tuple pair = new Tuple(msg.getProposer_seq(), msg.getValue().getLastOperation());

                if (verifyIfAllEq(instance.getAccept_ok_set(), pair)) {
                    instance.add_accept_ok(pair);
                } else if (verifyIfBigger(instance.getAccept_ok_set(), pair)) {
                    List<Tuple> list = new LinkedList<Tuple>();
                    list.add(pair);
                    instance.setAccept_ok_set(list);
                }
                if (instance.getDecided() == null && instance.getAccept_ok_set().size() >= (instance.getAll_processes().size() / 2) + 1) {
                    instance.setDecided(pair.getVal());
                    logger.debug("Decide at {} " + pair.getVal(),msg.getInstance());
                    triggerNotification(new DecidedNotification(msg.getInstance(), pair.getVal()));
                    decider++;
                    if (instance.getProposer_seq() == pair.getSeq())
                        cancelTimeout(msg.getInstance());

                }
            }
        }
    }

    private void uponAddReplica(AddReplicaRequest request, short sourceProto) {
        logger.debug("Received " + request);
        //The AddReplicaRequest contains an "instance" field, which we ignore in this incorrect protocol.
        //You should probably take it into account while doing whatever you do here.
        if(!membership.contains(request.getReplica())) {
            membership.add(request.getReplica());
            Set<Integer> keys = paxosInstancesMap.keySet();
            for (Integer key : keys) {
                if (key > request.getInstance()) {
                    PaxosInstance paxos = paxosInstancesMap.get(key);
                    if (paxos != null) {
                        List<Host> processes = paxos.getAll_processes();
                        processes.add(request.getReplica());
                        paxos.setAll_processes(processes);
                        paxosInstancesMap.put(key, paxos);
                    }
                }
            }
        }


        // adds the new host with no restriction if the list is empty
       /* boolean added = false;
        if (!membership.isEmpty()) {
            int index = 0;
            while (index < membership.size() && !added) {
                if (newHost.toString().compareTo(membership.get(index).toString()) < 0) {
                    membership.add(index, newHost);
                    added = true;
                }
            }
        }
        if (!added)
            membership.add(newHost);*/


    }

    private void uponRemoveReplica(RemoveReplicaRequest request, short sourceProto) {
        logger.debug("Received " + request);
        //The RemoveReplicaRequest contains an "instance" field, which we ignore in this incorrect protocol.
        //You should probably take it into account while doing whatever you do here.
        membership.remove(request.getReplica());
        Set<Integer> keys = paxosInstancesMap.keySet();
        for (Integer key : keys) {
            if (key > request.getInstance()) {
                PaxosInstance paxos = paxosInstancesMap.get(key);
                if (paxos != null) {
                    List<Host> processes = paxos.getAll_processes();
                    processes.remove(request.getReplica());
                    paxos.setAll_processes(processes);
                    paxosInstancesMap.put(key, paxos);
                }
            }
        }
    }


    private void uponMsgFail(ProtoMessage msg, Host host, short destProto, Throwable throwable, int channelId) {
        //If a message fails to be sent, for whatever reason, log the message and the reason
        logger.error("Message {} to {} failed, reason: {}", msg, host, throwable);
    }

    private void uponTimeout(Timeout timer, long timerID) {
        try{
            int InstanceId = timeoutInstancesMap.get(timerID);
            PaxosInstance instance = paxosInstancesMap.get(InstanceId);
            if (instance.getDecided() == null) {
                int nSeq = instance.getProposer_seq() + instance.getAll_processes().size();
                instance.setProposer_seq(nSeq);
                MPPrepareMessage MPPrepareMessage = new MPPrepareMessage(InstanceId, nSeq);
                for (Host h : instance.getAll_processes()) {
                    sendMessage(MPPrepareMessage, h);
                }
                instance.setPrepare_ok_set(new LinkedList<Tuple>());
                createTimeout(InstanceId);
                paxosInstancesMap.put(InstanceId, instance);
            }
        }catch (NullPointerException e ){
            logger.info("Timeout == null , instancia {}");
        }
    }

}
