package simpledb.buffer;

import simpledb.file.*;
import simpledb.server.SimpleDB;

public class BufferTest {
    public static void main(String[] args) {
        SimpleDB db = new SimpleDB("buffertest", 400, 3); // バッファ数は３
        BufferMgr bm = db.bufferMgr();

        Buffer buff1 = bm.pin(new BlockId("testfile", 1));
        Page p = buff1.contents();
        int n = p.getInt(80);
        p.setInt(80, n+1); // pin されている buff1 のページを更新してもすぐにはディスクに書かれない
        buff1.setModified(1, 0); // 更新されたフラグを立てても立てただけだと更新されない
        System.out.println("The new value is " + (n+1));
        bm.unpin(buff1);
        Buffer buff2 = bm.pin(new BlockId("testfile", 2));
        Buffer buff3 = bm.pin(new BlockId("testfile", 3));
        Buffer buff4 = bm.pin(new BlockId("testfile", 4)); // ここで空きバッファとして unpin されている buff1 を使うために書かれる

        bm.unpin(buff2);
        buff2 = bm.pin(new BlockId("testfile", 1)); // buff1 の場所に buff2 ページを設定
        Page p2 = buff2.contents();
        p2.setInt(80, 9999);
        buff2.setModified(1, 0); // ここでフラグを立ててもまだ強制的には更新されない
        bm.unpin(buff2); // unpin はしたが書き出しのイベントが無い
        // これで終了すると buff2 の更新は失われる
    }
}
