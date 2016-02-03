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
// source: google/api/label.proto

package com.google.api;

public interface LabelDescriptorOrBuilder extends
    // @@protoc_insertion_point(interface_extends:google.api.LabelDescriptor)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional string key = 1;</code>
   *
   * <pre>
   * The label key. Details are described [here](go/api-label#label_keys).
   * </pre>
   */
  java.lang.String getKey();
  /**
   * <code>optional string key = 1;</code>
   *
   * <pre>
   * The label key. Details are described [here](go/api-label#label_keys).
   * </pre>
   */
  com.google.protobuf.ByteString
      getKeyBytes();

  /**
   * <code>optional .google.api.LabelDescriptor.ValueType value_type = 2;</code>
   *
   * <pre>
   * The type of data that can be assigned to the label.
   * </pre>
   */
  int getValueTypeValue();
  /**
   * <code>optional .google.api.LabelDescriptor.ValueType value_type = 2;</code>
   *
   * <pre>
   * The type of data that can be assigned to the label.
   * </pre>
   */
  com.google.api.LabelDescriptor.ValueType getValueType();

  /**
   * <code>optional string description = 3;</code>
   *
   * <pre>
   * A human-readable description for the label.
   * </pre>
   */
  java.lang.String getDescription();
  /**
   * <code>optional string description = 3;</code>
   *
   * <pre>
   * A human-readable description for the label.
   * </pre>
   */
  com.google.protobuf.ByteString
      getDescriptionBytes();
}