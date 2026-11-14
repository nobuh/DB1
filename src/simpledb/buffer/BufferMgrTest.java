package simpledb.buffer;

import simpledb.file.*;
import simpledb.server.SimpleDB;

public class BufferMgrTest {
    public static void main(String[] args) throws Exception {
        SimpleDB db = new SimpleDB("buffermgrtest", 400, 3);
        BufferMgr bm = db.bufferMgr();

        Buffer[] buff = new Buffer[6];
        buff[0] = bm.pin(new BlockId("testfile", 0));
        buff[1] = bm.pin(new BlockId("testfile", 1));
        buff[2] = bm.pin(new BlockId("testfile", 2));
        bm.unpin(buff[1]); buff[1] = null;              // 即 unpin してフリーに
        buff[3] = bm.pin(new BlockId("testfile", 0));   // 同一物理ブロックへの pin なので新しくバッファを使わず既存のバッファの pin に +1 される 
        buff[4] = bm.pin(new BlockId("testfile", 1));   // 3つめのバッファとして使用
        System.out.println("Available buffers: " + bm.available());
        try {
            System.out.println("Attemping to pin block 3...");
            buff[5] = bm.pin(new BlockId("testfile", 3)); // 空きが無いので失敗する想定
        }
        catch (BufferAbortException e) {
            System.out.println("Exception: No available buffers\n");
        }
        bm.unpin(buff[2]); buff[2] = null;
        buff[5] = bm.pin(new BlockId("testfile", 3)); // これは可能

        System.out.println("Fatal Buffer Allocation:");
        for (int i=0; i < buff.length; i++) {
            Buffer b = buff[i];
            if (b != null) 
                System.out.println("buff["+i+"] pinned to block " + b.block());
        }
    }
}

