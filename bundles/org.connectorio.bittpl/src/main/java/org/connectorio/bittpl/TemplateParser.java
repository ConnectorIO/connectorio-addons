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

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.connectorio.bittpl.parser.BitTPLBaseListener;
import org.connectorio.bittpl.parser.BitTPLLexer;
import org.connectorio.bittpl.parser.BitTPLParser;
import org.connectorio.bittpl.parser.BitTPLParser.BitSeqContext;
import org.connectorio.bittpl.parser.BitTPLParser.HexSeqContext;
import org.connectorio.bittpl.parser.TemplateErrorListener;
import org.connectorio.bittpl.segment.Bit;
import org.connectorio.bittpl.segment.Element;
import org.connectorio.bittpl.segment.Nibble;
import org.connectorio.bittpl.segment.BitWildcard;
import org.connectorio.bittpl.segment.NibbleWildcard;

public class TemplateParser {

  public static Template parse(String template) {
    BitTPLLexer lexer = new BitTPLLexer(CharStreams.fromString(template));
    lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
    BitTPLParser parser = new BitTPLParser(new CommonTokenStream(lexer));
    parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
    parser.addErrorListener(new TemplateErrorListener());

    Listener listener = new Listener();
    new ParseTreeWalker().walk(listener, parser.segments());

    return new Template(listener.elements);
  }

  static class Listener extends BitTPLBaseListener {
    private final List<Element> elements = new ArrayList<>();

    private int offset = 0;
    private final static String STAR = "*";
    private final static String DOT = ".";

    @Override
    public void enterHexSeq(HexSeqContext ctx) {
      for (TerminalNode node : ctx.HEX_SEQ()) {
        String nibble = node.getText();
        if (STAR.equals(nibble)) {
          elements.add(new NibbleWildcard(offset));
        } else {
          elements.add(new Nibble(offset, nibble));
        }
        offset += 8;
      }
    }


    @Override
    public void enterBitSeq(BitSeqContext ctx) {
      for (TerminalNode node : ctx.BIT_SEQ()) {
        String bit = node.getText();
        if (DOT.equals(bit)) {
          elements.add(new BitWildcard(offset));
        } else {
          elements.add(new Bit(offset, bit.equals("1")));
        }
        offset += 1;
      }
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
      super.visitErrorNode(node);
      throw new IllegalArgumentException(node.toString());
    }
  }

}
