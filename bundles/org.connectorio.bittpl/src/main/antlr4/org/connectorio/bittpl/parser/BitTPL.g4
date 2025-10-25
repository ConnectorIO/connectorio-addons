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
grammar BitTPL;

// This grammar is a basic definition of permitted inputs for byte based messages.
// It allows to define a selective templates and matching patterns.

segments
    : segment* EOF
    ;

segment
    : ( binary | octet ) WS?
    ;

binary
    : 'b' bit=bitSeq
    ;

octet
    : 'x' hex=hexSeq
    ;


hexSeq : HEX_SEQ+;
bitSeq : BIT_SEQ+;

BIT_SEQ : BIT | BIT_ANY;
BIT_ANY : '.';
BIT
    : '0'
    | '1'
    ;

HEX_SEQ : HEX_DIGIT | HEX_ANY;
HEX_ANY : '*';
HEX_DIGIT
    : '0' .. '9'
    | 'a' .. 'f'
    | 'A' .. 'F'
    ;


WS
    : ( ' ' ) -> channel ( HIDDEN )
    ;