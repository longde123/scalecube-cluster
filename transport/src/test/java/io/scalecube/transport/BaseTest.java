package io.scalecube.transport;

import static io.scalecube.transport.TransportConfig.DEFAULT_MAX_FRAME_LENGTH;

import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/** Base test class. */
public class BaseTest {

  protected static final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);

  @BeforeEach
  public final void baseSetUp(TestInfo testInfo) {
    LOGGER.info("***** Test started  : " + testInfo.getDisplayName() + " *****");
  }

  @AfterEach
  public final void baseTearDown(TestInfo testInfo) {
    LOGGER.info("***** Test finished : " + testInfo.getDisplayName() + " *****");
  }

  /**
   * Sending message from src to destination.
   *
   * @param transport src
   * @param to destination
   * @param msg request
   */
  protected Mono<Void> send(Transport transport, Address to, Message msg) {
    return transport
        .send(to, msg)
        .doOnError(
            th ->
                LOGGER.error(
                    "Failed to send {} to {} from transport: {}, cause: {}",
                    msg,
                    to,
                    transport,
                    th.toString()));
  }

  /**
   * Stopping transport.
   *
   * @param transport trnasport object
   */
  protected void destroyTransport(Transport transport) {
    if (transport != null && !transport.isStopped()) {
      try {
        transport.stop().block(Duration.ofSeconds(1));
      } catch (Exception ignore) {
        LOGGER.warn("Failed to await transport termination");
      }
    }
  }

  /**
   * Factory method to create a transport.
   *
   * @return tramsprot
   */
  protected Transport createTransport() {
    TransportConfig config =
        TransportConfig.builder()
            .connectTimeout(100)
            .useNetworkEmulator(true)
            .maxFrameLength(DEFAULT_MAX_FRAME_LENGTH)
            .build();

    Transport transport = Transport.bindAwait(config);

    return new SenderAwareTransport(transport);
  }
}
