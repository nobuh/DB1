package simpledb.log;

import java.util.Iterator;

import simpledb.file.*;

public class LogMgr {
    private FileMgr fm;
    private String logfile;
    private Page logpage;
    private BlockId currentblk;
    private int latestLSN = 0;
    private int lastSavedLSN = 0;

    public LogMgr(FileMgr fm, String logfile) {
        this.fm = fm;
        this.logfile = logfile;
        byte[] b = new byte[fm.blockSize()];
        logpage = new Page(b);
        // logsize は logfile 内のブロック数
        int logsize = fm.length(logfile);
        if (logsize == 0)
            currentblk = appendNewBlock();
        else {
            // logfile中の末端のブロックに参照できるリファレンスを設定
            currentblk = new BlockId(logfile, logsize-1);
            // log 用固定ページへそのブロックを読み込み
            fm.read(currentblk, logpage);
        }
    }

    public void flush(int lsn) {
        if (lsn >= lastSavedLSN)
            flush();
    }

    public Iterator<byte[]> iterator() {
        flush();
        return new LogIterator(fm, currentblk);
    }

    // ログのレコードをページ内で追加する
    // レコードは先頭にサイズ、データの中には LSN を持っている
    // 降順でのスキャンに備えて、ページ内では最初のレコードを末尾
    // 以後前に向かって詰めていく方式
    // 格納の先頭位置はページの先頭に「境界値」として保持
    // append の結果は更新された latestLSN を返している
    public synchronized int append(byte[] logrec) {
        int boundary = logpage.getInt(0);
        int recsize = logrec.length;
        // レコードは先頭にそのサイズを持っているので Integer + レコードになる
        int bytesneeded = recsize + Integer.BYTES;
        // 前に進めた先頭位置が、境界値用のインデクス 0 にかかるようなら
        // ページを書いてしまって
        // 新しいブロックを用意し、
        if (boundary - bytesneeded < Integer.BYTES) { // doesn't fit
            flush();
            currentblk = appendNewBlock();
            boundary = logpage.getInt(0);
        }
        int recpos = boundary - bytesneeded;
        logpage.setBytes(recpos, logrec);
        // 境界値に相当する場所を更新
        logpage.setInt(0, recpos);
        latestLSN += 1;
        // latestLSN はプロパティで保持されているが、トランザクションなどでも
        // 要求に対して対応する値を直接的に利用するので
        // メソッドの返り値になっているのは必須の仕組み
        return latestLSN;
    }

    private BlockId appendNewBlock() {
        BlockId blk = fm.append(logfile);
        logpage.setInt(0, fm.blockSize());
        fm.write(blk, logpage);
        return blk;
    }

    private void flush() {
        // logpage をcurrentblk で示されたファイル上のブロックへ書き出す
        fm.write(currentblk, logpage);
        // ページ上にある最新レコードのLSNを保存したLSNとする
        lastSavedLSN = latestLSN;
    }
}


