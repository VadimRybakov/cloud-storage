package ru.geekbrains.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;
import ru.geekbrains.common.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private NettyServer server;
    static ConcurrentLinkedDeque<ChannelHandlerContext> clients = new ConcurrentLinkedDeque<>();
    private static final String STORAGE = "./server/data";
    private static final Logger log = Logger.getLogger(NettyServerHandler.class);
    private String fileName;
    private FileMessage file;
    private String login;
    private String password;

    public NettyServerHandler() {
        server = new NettyServer();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        if (msg == null) return;
        if (msg instanceof User) {
            User user = (User) msg;
            login = user.getLogin();
            password = user.getPassword();
            clients.add(ctx);
            log.info("new client connected " + login);
        }
        if(msg instanceof String)
            fileName = (String) msg;
        if(msg instanceof FileMessage) {
            file = (FileMessage) msg;
            log.info(file + " is received");
        }
        if(msg instanceof State) {
            State state = (State) msg;

            if (state == State.AUTH) {
                if(SqlClient.getId(login, password) != 0)
                    state = State.LIST_REQUEST;
                else {
                    clients.remove(ctx);
                    log.info("login or password is incorrect");
                    ctx.close();
                }
            }
            if (state == State.LIST_REQUEST) {
                log.info("server sends FileList");
                Path path = Paths.get(STORAGE);
                List<FileMessage> fl = Files.list(path).map(FileMessage::new).collect(Collectors.toList());
                FileList fileList = new FileList(fl, STORAGE);
                ctx.writeAndFlush(fileList);
            }
            if (state == State.FILE_DOWNLOAD) {
                log.info("server sends file " + fileName);
                FileMessage fileMessage = new FileMessage(Paths.get(STORAGE + "/" + fileName));
                ctx.writeAndFlush(fileMessage);
            }
            if(state == State.FILE_DELETE) {
                log.info("delete file on server: " + fileName);
                Files.delete(Paths.get(STORAGE + "/" + fileName));
            }
            if(state == State.FILE_RECEIVE) {
                Files.write(Paths.get(STORAGE + "/" + file.getFilename()), file.getFileByteArray(), CREATE, WRITE, APPEND);
                log.info("received file on server " + fileName);
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
