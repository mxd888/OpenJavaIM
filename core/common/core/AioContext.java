package org.ojim.core.common.core;


import org.ojim.core.server.AioServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Semaphore;

/**
 * Created by DELL(mxd) on 2021/12/22 15:38
 */
public class AioContext implements SystemConst{

    private static final Logger log = LoggerFactory.getLogger(AioContext.class);

    private AsynchronousSocketChannel clientSocketChannel;

    private ReadCompletionHandler readCompletionHandler;

    private WriteCompletionHandler writeCompletionHandler;

    private ByteBuffer readBuffer;

    private ByteBuffer writeBuffer;

    public AioConfig config;

    protected final Semaphore writeSemaphore = new Semaphore(Semaphore);

    protected final Semaphore readSemaphore = new Semaphore(Semaphore);

    private static final int MAX_WRITE_SIZE = 256 * 1024;

    public AioContext(AsynchronousSocketChannel clientSocketChannel, ReadCompletionHandler readCompletionHandler,
                      WriteCompletionHandler writeCompletionHandler, AioConfig config) {
        this.clientSocketChannel = clientSocketChannel;
        this.readCompletionHandler = readCompletionHandler;
        this.writeCompletionHandler = writeCompletionHandler;
        this.config = config;
        this.readBuffer = ByteBuffer.allocate(capacity);
    }

    public AioContext(){

    }

    public void initContext() {
        if (config.isServer()){
            log.info("服务端初始化用户上下文...");
        }

        readSemaphore.tryAcquire();
        continueRead();

    }

    public void close() {
        if (clientSocketChannel != null){
            try {
                clientSocketChannel.close();
            }catch (IOException e){
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void readFromChannel() {
        readBuffer.flip();

        // 解决粘包
        while (readBuffer.hasRemaining()) {
            Packet decode;
            try {
                // 解码
                decode = config.aioHandler.decode(readBuffer, this);
            } catch (Exception e) {
                System.err.println("AioContext 83:" + e.getMessage());
                throw e;
            }
            if (decode == null) {
                // 发生半包
                break;
            }
            //消息处理器
            try {
                if (this.config instanceof AioServerConfig){
                    ((AioServerConfig) this.config).monitor.massageTotal();
                }
                config.aioHandler.handle(this, decode);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                throw e;
            }
        }
        //数据读取完毕
        if (readBuffer.remaining() == 0) {
            readSemaphore.release();
            readBuffer.clear();
        } else if (readBuffer.position() > 0) {
            // 仅当发生数据读取时调用compact,减少内存拷贝  继续读  解决半包
            // 该方法就是将 position 到 limit 之间还未读取的数据拷贝到 ByteBuffer 中数组的最前面，
            // 然后再将 position 移动至这些数据之后的一位，将 limit 移动至 capacity。
            // 这样 position 和 limit 之间就是已经读取过的老的数据或初始化的数据
            readBuffer.compact();
        } else {
            readBuffer.position(readBuffer.limit());
            readBuffer.limit(readBuffer.capacity());
        }
        try {
            readSemaphore.acquire();
            continueRead();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public final void write(Packet packet){
        ByteBuffer encode = config.aioHandler.encode(this, packet);
        try {
            write(encode);
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }
    
    public final void write(final ByteBuffer buffer) throws IOException {

        if (!buffer.hasRemaining()) {
            throw new InvalidObjectException("buffer has no remaining");
        }
        if (config.writeCacheQueue.size() <= 0) {
            try {
                writeSemaphore.acquire();
                writeBuffer = buffer;
                continueWrite();
            } catch (InterruptedException e) {
                log.error("acquire fail", e);
                Thread.currentThread().interrupt();
                throw new IOException(e.getMessage());
            }
            return;
        } else if ((writeSemaphore.tryAcquire())) {
            writeBuffer = buffer;
            continueWrite();
            return;
        }
        try {
            //正常存取
            config.writeCacheQueue.put(buffer);
        } catch (InterruptedException e) {
            log.error("put buffer into cache fail", e);
            Thread.currentThread().interrupt();
        }
        if (writeSemaphore.tryAcquire()) {
            writeToChannel();
        }
    }

    void writeToChannel() {
        if (writeBuffer != null && writeBuffer.hasRemaining()) {
            continueWrite();
            return;
        }

        if (config.writeCacheQueue == null || config.writeCacheQueue.size() == 0) {
            if (writeBuffer != null && writeBuffer.isDirect()) {
                DirectBufferUtil.offerFirstTemporaryDirectBuffer(writeBuffer);
            }
            writeBuffer = null;
            writeSemaphore.release();
            if (config.writeCacheQueue != null && config.writeCacheQueue.size() > 0 && writeSemaphore.tryAcquire()) {
                writeToChannel();
            }
            return;
        }
        int totalSize = config.writeCacheQueue.expectRemaining(MAX_WRITE_SIZE);
        ByteBuffer headBuffer = config.writeCacheQueue.poll();
        assert headBuffer != null;
        if (headBuffer.remaining() == totalSize) {
            writeBuffer = headBuffer;
        } else {
            if (writeBuffer == null || totalSize > writeBuffer.capacity()) {
                if (writeBuffer != null && writeBuffer.isDirect()) {
                    DirectBufferUtil.offerFirstTemporaryDirectBuffer(writeBuffer);
                }
                writeBuffer = DirectBufferUtil.getTemporaryDirectBuffer(totalSize);
            } else {
                writeBuffer.clear().limit(totalSize);
            }
            writeBuffer.put(headBuffer);
            config.writeCacheQueue.pollInto(writeBuffer);
            writeBuffer.flip();
        }

        continueWrite();

    }

    protected void continueRead() {

        readFromChannel0(readBuffer);
    }

    protected void continueWrite() {
        writeToChannel0(writeBuffer);
    }

    protected final void readFromChannel0(ByteBuffer buffer) {
        clientSocketChannel.read(buffer, this, readCompletionHandler);
    }

    protected final void writeToChannel0(ByteBuffer buffer) {
        clientSocketChannel.write(buffer, this, writeCompletionHandler);
    }
}
