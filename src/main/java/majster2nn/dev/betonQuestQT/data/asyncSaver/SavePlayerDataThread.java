package majster2nn.dev.betonQuestQT.data.asyncSaver;

import majster2nn.dev.betonQuestQT.data.DataBaseHandler;

import java.util.ArrayList;
import java.util.List;

public class SavePlayerDataThread extends Thread {

    private final List<Record> queue = new ArrayList<>();
    private boolean running = true;
    private long previousTime = 0;
    private final long milisInterval = 5000;
    private DataBaseHandler privateHandler;

    public SavePlayerDataThread() {
        setName("SavePlayerDataQueue");
    }

    @Override
    public void run() {
//        while(true){
//            while(queue.isEmpty()){
//                if(!running){
//                    BetonQuestQT.getInstance().dataBaseHandler.close();
//                    return;
//                }
//
//                synchronized (this){
//                    try{
//                        wait();
//                    } catch (InterruptedException e) {
//                        BetonQuestQT.getInstance().getLogger().warning("Something interrupted data saver.");
//                    }
//                }
//            }
//            long now = System.currentTimeMillis();
//            if(now - previousTime >= milisInterval){
//                if(BetonQuestQT.debug) {
//                    System.out.println("Saving data! " + " " + queue.getFirst().value + " " + queue.getFirst().column + " " + queue.getFirst().key);
//                }
//                previousTime = now;
//
//                List<Record> snapshot;
//                synchronized (this) {
//                    snapshot = new ArrayList<>(queue);
//                    queue.clear();
//                }
//
//                if(running) {
//                    BetonQuestQT.getInstance().dataBaseHandler.saveToDb("userData", snapshot);
//                }else{
//                    privateHandler.saveToDb("userData", snapshot);
//                }
//            }
//        }
    }

    public void addRecordToQueue(Record record){
        synchronized (this) {
            queue.add(record);
            notify();
        }
    }

    public void end(DataBaseHandler handler){
        synchronized (this){
            privateHandler = handler;
            running = false;
        }
    }
}
