/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.bittpl;

import com.github.jinahya.bit.io.ArrayByteInput;
import com.github.jinahya.bit.io.ArrayByteOutput;
import com.github.jinahya.bit.io.BitInput;
import com.github.jinahya.bit.io.DefaultBitInput;
import com.github.jinahya.bit.io.DefaultBitOutput;
import java.io.IOException;
import java.util.List;
import org.connectorio.bittpl.segment.Element;

public class Template {

  protected final List<Element> elements;
  private final int bitLength;

  public Template(List<Element> elements) {
    this.elements = elements;
    int count = 0;
    for (Element element : elements) {
      count += element.length();
    }
    this.bitLength = count;
  }

  public boolean matches(byte[] seq) {
    ArrayByteInput byteInput = new ArrayByteInput(seq);
    BitInput input = new DefaultBitInput(byteInput);

    // iterate over elements
    for (int index = 0; index < elements.size(); index++) {
      Element element = elements.get(index);
      try {
        boolean matches = element.matches(input);
        if (!matches) {
          return false;
        }
      } catch (IOException e) {
        return false;
      }
    }

    return true;
  }

  public byte[] toMessage() throws IOException {
    byte[] target = new byte[bitLength / 8];
    DefaultBitOutput output = new DefaultBitOutput(new ArrayByteOutput(target));
    for (Element element : elements) {
      element.write(output);
    }
    return target;
  }

}
