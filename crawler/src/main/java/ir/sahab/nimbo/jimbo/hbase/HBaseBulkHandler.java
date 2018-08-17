package ir.sahab.nimbo.jimbo.hbase;

import ir.sahab.nimbo.jimbo.main.Logger;
import org.apache.hadoop.hbase.client.Put;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import static ir.sahab.nimbo.jimbo.main.Config.HBASE_BULK_LIMIT;

public class HBaseBulkHandler implements Runnable {
    private final ArrayBlockingQueue<HBaseDataModel> bulkQueue;
    private final List<Put> puts = new ArrayList<>();

    public HBaseBulkHandler(ArrayBlockingQueue<HBaseDataModel> bulkQueue) {
        this.bulkQueue = bulkQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                for (int i = 0; i < HBASE_BULK_LIMIT; i++) {
                    puts.add(HBase.getInstance().getPutData(bulkQueue.take()));
                }
                HBase.getInstance().getTable().put(puts);
                puts.clear();
            } catch (InterruptedException | IOException e) {
                Logger.getInstance().debugLog(e.getMessage());
            }
        }
    }
}
