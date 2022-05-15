package chap03;

import chap01.EchoServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class EchoServerV2 {
    public static void main(String[] args) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // EventLoopGroup 인터페이스에 NioEventLoopGroup 클래스의 객체를 할당한다.
        // 생성자에 입력된 스레드의 수가 1이므로 단일 스레드로 동작하는 NioEventLoopGroup 객체를 생성한다.
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        // EventLoopGroup 인터페이스에 NioEventLoopGroup 클래스의 객체를 할당한다.
        // 생성자에 인수가 없으므로 CPU 코어 수에 따른 스레드 수가 설정된다.
        try{
            ServerBootstrap b = new ServerBootstrap();
            // ServerBootstrap을 생성한다.
            b.group(bossGroup, workerGroup)
                    // 첫번째 인수(bossGroup)은 부모스레드이다.
                    // 부모스레드는 클라이언트 연결 요청의 수락을 담당한다.
                    // 두번째 인수(workerGroup)은 자식스레드이다.
                    // 자식스레드는 연결된 소켓에 대한 I/O 처리를 담당하는 자식스레드이다.
                    .channel(NioServerSocketChannel.class)
                    // 서버 소켓(부모 스레드)가 사용할 네트워크 입출력 모드를 설정한다.
                    // 여기서는 NioServerSocketChannel 클래스를 설정했기 때문에 NIO 모드로 동작한다.
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // ServerBootstrap의 handler 메서드로 LoggingHandler를 설정하는 방법
                    // LoggingHandler는 네티에서 기본으로 제공하는 코덱
                    // 서버 소켓 채널 에서 발생한 이벤트만을 처리하기 때문에
                    // 채널에서 발생하는 양방향 이벤트를 로그로 출력하지 않는다.
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // 자식 채널의 초기화 방법을 설정한다. 여기서는 익명클래스로 채널 초기화 방법을 지정했다.
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // ChannelInitializer는 클라이언트로부터 연결된 채널이 초기화될 때의 기본 동작이 지정된 추상 클래스이다.
                            ChannelPipeline p = socketChannel.pipeline();
                            // 채널 파이프라인 객체를 생성한다.
                            p.addLast(new EchoServerHandler());
                            // 채널 파이프라인에 EchoServerHandler 클래스를 등록한다.
                            // EchoServerHandler 클래스는 이후에 클라이언트의 연결이 생성되었을 때 데이터 처리를 담당한다.
                        }
                    });

            ChannelFuture f = b.bind(8888).sync();

            f.channel().closeFuture().sync();
        }
        finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
