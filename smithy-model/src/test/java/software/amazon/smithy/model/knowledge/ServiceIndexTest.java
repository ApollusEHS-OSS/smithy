/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.smithy.model.knowledge;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;

import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.HttpBasicAuthTrait;
import software.amazon.smithy.model.traits.HttpBearerAuthTrait;
import software.amazon.smithy.model.traits.HttpDigestAuthTrait;
import software.amazon.smithy.model.traits.Trait;

public class ServiceIndexTest {

    private static Model model;

    @BeforeAll
    public static void before() {
        model = Model.assembler()
                .addImport(ServiceIndexTest.class.getResource("service-index-finds-auth-schemes.smithy"))
                .assemble()
                .unwrap();
    }

    @AfterAll
    public static void after() {
        model = null;
    }

    @Test
    public void returnsProtocolsOfService() {
        Model model = Model.assembler()
                .addImport(getClass().getResource("service-index-loads-protocols.smithy"))
                .assemble()
                .unwrap();
        ServiceIndex serviceIndex = model.getKnowledge(ServiceIndex.class);
        Map<ShapeId, Trait> protocols = serviceIndex.getProtocols(ShapeId.from("smithy.example#TestService"));

        assertThat(protocols, hasKey(ShapeId.from("smithy.example#fooJson")));
        assertThat(protocols, hasKey(ShapeId.from("smithy.example#fooXml")));
        assertThat(protocols.keySet(), hasSize(2));
    }

    @Test
    public void returnsAuthSchemesOfService() {
        ServiceIndex serviceIndex = model.getKnowledge(ServiceIndex.class);
        Map<ShapeId, Trait> auth = serviceIndex.getAuthSchemes(
                ShapeId.from("smithy.example#ServiceWithNoAuthTrait"));

        assertThat(auth.keySet(), hasSize(3));
        assertThat(auth, hasKey(HttpBasicAuthTrait.ID));
        assertThat(auth, hasKey(HttpDigestAuthTrait.ID));
        assertThat(auth, hasKey(HttpBearerAuthTrait.ID));
    }

    @Test
    public void getsAuthSchemesOfServiceWithNoAuthTrait() {
        ServiceIndex serviceIndex = model.getKnowledge(ServiceIndex.class);
        Map<ShapeId, Trait> auth = serviceIndex.getEffectiveAuthSchemes(
                ShapeId.from("smithy.example#ServiceWithNoAuthTrait"));

        assertThat(auth.keySet(), hasSize(3));
        assertThat(auth, hasKey(HttpBasicAuthTrait.ID));
        assertThat(auth, hasKey(HttpDigestAuthTrait.ID));
        assertThat(auth, hasKey(HttpBearerAuthTrait.ID));
    }

    @Test
    public void getsAuthSchemesOfServiceWithAuthTrait() {
        ServiceIndex serviceIndex = model.getKnowledge(ServiceIndex.class);
        Map<ShapeId, Trait> auth = serviceIndex.getEffectiveAuthSchemes(
                ShapeId.from("smithy.example#ServiceWithAuthTrait"));

        assertThat(auth.keySet(), hasSize(2));
        assertThat(auth, hasKey(HttpBasicAuthTrait.ID));
        assertThat(auth, hasKey(HttpDigestAuthTrait.ID));
    }

    @Test
    public void getsAuthSchemesOfOperationWithNoAuthTraitAndServiceWithNoAuthTrait() {
        ServiceIndex serviceIndex = model.getKnowledge(ServiceIndex.class);
        Map<ShapeId, Trait> auth = serviceIndex.getEffectiveAuthSchemes(
                ShapeId.from("smithy.example#ServiceWithNoAuthTrait"),
                ShapeId.from("smithy.example#OperationWithNoAuthTrait"));

        assertThat(auth.keySet(), hasSize(3));
        assertThat(auth, hasKey(HttpBasicAuthTrait.ID));
        assertThat(auth, hasKey(HttpDigestAuthTrait.ID));
        assertThat(auth, hasKey(HttpBearerAuthTrait.ID));
    }

    @Test
    public void getsAuthSchemesOfOperationWithNoAuthTraitAndServiceWithAuthTrait() {
        ServiceIndex serviceIndex = model.getKnowledge(ServiceIndex.class);
        Map<ShapeId, Trait> auth = serviceIndex.getEffectiveAuthSchemes(
                ShapeId.from("smithy.example#ServiceWithAuthTrait"),
                ShapeId.from("smithy.example#OperationWithNoAuthTrait"));

        assertThat(auth.keySet(), hasSize(2));
        assertThat(auth, hasKey(HttpBasicAuthTrait.ID));
        assertThat(auth, hasKey(HttpDigestAuthTrait.ID));
    }

    @Test
    public void getsAuthSchemesOfOperationWithAuthTrait() {
        ServiceIndex serviceIndex = model.getKnowledge(ServiceIndex.class);
        Map<ShapeId, Trait> auth = serviceIndex.getEffectiveAuthSchemes(
                ShapeId.from("smithy.example#ServiceWithAuthTrait"),
                ShapeId.from("smithy.example#OperationWithAuthTrait"));

        assertThat(auth.keySet(), hasSize(1));
        assertThat(auth, hasKey(HttpDigestAuthTrait.ID));
    }

    @Test
    public void returnsAnEmptyCollectionWhenTheServiceDoesNotExist() {
        ServiceIndex serviceIndex = model.getKnowledge(ServiceIndex.class);
        Map<ShapeId, Trait> auth = serviceIndex.getEffectiveAuthSchemes(
                ShapeId.from("smithy.example#Invalid"),
                ShapeId.from("smithy.example#OperationWithAuthTrait"));

        assertThat(auth.keySet(), empty());
    }
}
