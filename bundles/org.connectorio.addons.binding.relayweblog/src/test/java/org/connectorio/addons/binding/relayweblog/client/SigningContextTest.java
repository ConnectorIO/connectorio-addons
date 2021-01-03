/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.binding.relayweblog.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SigningContextTest {

  // default
  String password = "00001767";

  String session = "a8e8d24c1a203c6f193a954212a60ec69d03a61b04f2ac524242f522f486cba4eb06dd860e4aeeac9e0cf466ec0b19e4006"
    + "c64c77c8be2b579479a385a1f9639fc1ce6a755bb8dbc18522c9d65c8bc27de672f1a19e5b9bf3f37d81db24f79fa07802e5ab5664b4aa1c"
    + "43f8b9729f0843611add9a5978041a99d651d87264c18e8c6be590a95702460e126f451dd76159e7160f130b2599e4c0a1defccd784e11b4"
    + "faa35ddd70651ff4e285bb2b573c56e21afa64e8149198594aa6965c9888dd036106f210974dda1d50415d29d9d09555dcf9fa7291dafc8f"
    + "9a2b9789dbaadf870a1058a8dafc18fcdd4784d5e34e74b3b1d0f04c3c6adfd6f0fbaa23d007fb55c40e3527c7698d5552b95255ced9c9ba"
    + "cde9e68ce1ea3664bb31ae7bd3efae93ae2ce04e62e805d342afaed6d45c783da2d10e9950a82db28eab60dc7ec27739f92bfa0c12271796"
    + "4c7bee37d793a6b7095fa9c7b2ba99fd9ad33ac3e39c6be3df167b4915885baf13615ae9b23b256301089399dedddf8b5957be7bb2228f1f"
    + "ec073b093d2c969beed36cc364242137b5d56e82a6936fdc8c40467cc67bab44c2f69e0c0dbc109191bd9c550de1962f6fa44a3aeb848319"
    + "02da2799fe5d305518b723283d960fa6f151d8f4b9fe448c41ba6585bef1860d7f1787258a0e061f336fdb7696f307b53ba83349d03ea7a4"
    + "0370613aa7525fc87119b5cb10a64";

  @Test
  public void verifySignatureLogic() {
    String passwordHash = Hash.sha512(password);

    String hmacSecret = "1eedbfcd44dada1105579e5fca9fbb868f8a17c408bd823410757eddba369b6250d3739ee2326efdab65997376faead3ec0499a3f79deac36e0d98146a62a965";
    // make sure that we hashed password
    assertThat(passwordHash).isNotEqualTo(hmacSecret);

    String headerHash = "1a81cebd3fd4db1057399e6755e61162ec29ce45bbba51ac1c63bee2c2560b9908091359303525ee753c76bcd8d12d65668193c473217151b5798c02beb746e7";

    String random = "b1a6214a62009514585a5ad0af320081";
    long microtime = 1608554870L;

    String payload = String.format("{\"payload\":\"%s\",\"password\":\"%s\"}", random, passwordHash);

    SigningContext context = new SigningContext(passwordHash);
    String hash = context.getHash("/api/v1/auth/ops/login", microtime, payload);

    assertThat(context.getHmacKey()).isEqualTo(hmacSecret);
    assertThat(hash).isEqualTo(headerHash);

    // second request

    microtime = 1608554871L;
    hmacSecret = "b4502ebfe57b0c714f9eb3f1b6496b846aaf6004ecb967d18e89efd334fef04cf7d16462e727e9beb9ff78d58ad192ffa61835595e4a3fbc569eac5cd2a05b80";
    context.setSession(session);
    assertThat(context.getHmacKey()).isEqualTo(hmacSecret);

    hash = context.getHash("/api/v1/domain/meterlist", microtime);
    headerHash = "43e8e3347a28a2345b63bfab06866bc938435f6d7af1fb737b68b974488a14d9642f1e7c0c7caafc2b20f1d648321790fab07f0934b4922f348a02f2d80090e9";
    assertThat(hash).isEqualTo(headerHash);
  }

}