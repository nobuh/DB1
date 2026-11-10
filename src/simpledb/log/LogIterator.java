package simpledb.log;

import java.util.Iterator;
import simpledb.file.*;

class LogIterator implements Iterator<byte[]> {
   private FileMgr fm;
   private BlockId blk;
   private Page p;
   private int currentpos;
   private int boundary;

   public LogIterator(FileMgr fm, BlockId blk) {
      this.fm = fm;
      this.blk = blk;
      byte[] b = new byte[fm.blockSize()];
      p = new Page(b);
      moveToBlock(blk);
   }

   public boolean hasNext() {
       // 先頭ブロックに達してなく、また
       // ポジションも末尾（時刻的には先頭）に達していないならまだレコードはある
      return currentpos<fm.blockSize() || blk.number()>0;
   }

   public byte[] next() {
      // 現在のポジションが末尾の場合ブロックは出来たばかりの空なので
      // その前のブロックへ参照し直す
      if (currentpos == fm.blockSize()) {
         blk = new BlockId(blk.fileName(), blk.number()-1);
         moveToBlock(blk);
      }
      // 現在位置のレコードを読む
      byte[] rec = p.getBytes(currentpos);
      // 逆順に入ってるので、前のレコードは、読んだ後ろについている
      currentpos += Integer.BYTES + rec.length;
      return rec;
   }

   // 指定された blk の内容を page に読み込み
   // 最終レコードの先頭を格納した boundary に位置を合わせる
   private void moveToBlock(BlockId blk) {
      fm.read(blk, p);
      boundary = p.getInt(0);
      currentpos = boundary;
   }
}
