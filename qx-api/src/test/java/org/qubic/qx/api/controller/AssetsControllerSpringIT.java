package org.qubic.qx.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.domain.Asset;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

@Slf4j
@SpringBootTest
class AssetsControllerSpringIT {

    private WebTestClient client;

    @BeforeEach
    public void setUpTestNode(WebApplicationContext context) {
        client = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .baseUrl("/service/v1/qx")
                .build();
    }

    @Test
    void getAssets() {
        client.get().uri("/assets")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Asset.class)
                .contains(Asset.builder().issuer("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB").name("QX").build())
                .contains(Asset.builder().issuer("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB").name("RANDOM").build())
                .contains(Asset.builder().issuer("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB").name("QUTIL").build())
                .contains(Asset.builder().issuer("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB").name("QTRY").build())
                .contains(Asset.builder().issuer("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB").name("MLM").build())
                .contains(Asset.builder().issuer("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB").name("QPOOL").build())
                .contains(Asset.builder().issuer("TFUYVBXYIYBVTEMJHAJGEJOOZHJBQFVQLTBBKMEHPEVIZFXZRPEYFUWGTIWG").name("QFT").build())
                .contains(Asset.builder().issuer("CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL").name("CFB").build())
                .contains(Asset.builder().issuer("QWALLETSGQVAGBHUCVVXWZXMBKQBPQQSHRYKZGEJWFVNUFCEDDPRMKTAUVHA").name("QWALLET").build());
    }

}