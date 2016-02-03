/*
 * Copyright 2015, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: google/api/error.proto

package com.google.api;

public interface CustomErrorOrBuilder extends
    // @@protoc_insertion_point(interface_extends:google.api.CustomError)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .google.api.CustomErrorRule rules = 1;</code>
   *
   * <pre>
   * The list of custom error rules to select to which messages this should
   * apply.
   * </pre>
   */
  java.util.List<com.google.api.CustomErrorRule> 
      getRulesList();
  /**
   * <code>repeated .google.api.CustomErrorRule rules = 1;</code>
   *
   * <pre>
   * The list of custom error rules to select to which messages this should
   * apply.
   * </pre>
   */
  com.google.api.CustomErrorRule getRules(int index);
  /**
   * <code>repeated .google.api.CustomErrorRule rules = 1;</code>
   *
   * <pre>
   * The list of custom error rules to select to which messages this should
   * apply.
   * </pre>
   */
  int getRulesCount();
  /**
   * <code>repeated .google.api.CustomErrorRule rules = 1;</code>
   *
   * <pre>
   * The list of custom error rules to select to which messages this should
   * apply.
   * </pre>
   */
  java.util.List<? extends com.google.api.CustomErrorRuleOrBuilder> 
      getRulesOrBuilderList();
  /**
   * <code>repeated .google.api.CustomErrorRule rules = 1;</code>
   *
   * <pre>
   * The list of custom error rules to select to which messages this should
   * apply.
   * </pre>
   */
  com.google.api.CustomErrorRuleOrBuilder getRulesOrBuilder(
      int index);

  /**
   * <code>repeated string types = 2;</code>
   *
   * <pre>
   * The list of custom error detail types, e.g. 'google.foo.v1.CustomError'.
   * </pre>
   */
  com.google.protobuf.ProtocolStringList
      getTypesList();
  /**
   * <code>repeated string types = 2;</code>
   *
   * <pre>
   * The list of custom error detail types, e.g. 'google.foo.v1.CustomError'.
   * </pre>
   */
  int getTypesCount();
  /**
   * <code>repeated string types = 2;</code>
   *
   * <pre>
   * The list of custom error detail types, e.g. 'google.foo.v1.CustomError'.
   * </pre>
   */
  java.lang.String getTypes(int index);
  /**
   * <code>repeated string types = 2;</code>
   *
   * <pre>
   * The list of custom error detail types, e.g. 'google.foo.v1.CustomError'.
   * </pre>
   */
  com.google.protobuf.ByteString
      getTypesBytes(int index);
}