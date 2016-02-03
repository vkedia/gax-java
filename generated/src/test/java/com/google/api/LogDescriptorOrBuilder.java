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
// source: google/api/log.proto

package com.google.api;

public interface LogDescriptorOrBuilder extends
    // @@protoc_insertion_point(interface_extends:google.api.LogDescriptor)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional string name = 1;</code>
   *
   * <pre>
   * The name of the log. It must be less than 512 characters long and can
   * include the following characters: upper- and lower-case alphanumeric
   * characters [A-Za-z0-9], and punctuation characters including
   * slash, underscore, hyphen, period [/_-.].
   * </pre>
   */
  java.lang.String getName();
  /**
   * <code>optional string name = 1;</code>
   *
   * <pre>
   * The name of the log. It must be less than 512 characters long and can
   * include the following characters: upper- and lower-case alphanumeric
   * characters [A-Za-z0-9], and punctuation characters including
   * slash, underscore, hyphen, period [/_-.].
   * </pre>
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>repeated .google.api.LabelDescriptor labels = 2;</code>
   *
   * <pre>
   * The set of labels that are available to describe a specific log entry.
   * Runtime requests that contain labels not specified here are
   * considered invalid.
   * </pre>
   */
  java.util.List<com.google.api.LabelDescriptor> 
      getLabelsList();
  /**
   * <code>repeated .google.api.LabelDescriptor labels = 2;</code>
   *
   * <pre>
   * The set of labels that are available to describe a specific log entry.
   * Runtime requests that contain labels not specified here are
   * considered invalid.
   * </pre>
   */
  com.google.api.LabelDescriptor getLabels(int index);
  /**
   * <code>repeated .google.api.LabelDescriptor labels = 2;</code>
   *
   * <pre>
   * The set of labels that are available to describe a specific log entry.
   * Runtime requests that contain labels not specified here are
   * considered invalid.
   * </pre>
   */
  int getLabelsCount();
  /**
   * <code>repeated .google.api.LabelDescriptor labels = 2;</code>
   *
   * <pre>
   * The set of labels that are available to describe a specific log entry.
   * Runtime requests that contain labels not specified here are
   * considered invalid.
   * </pre>
   */
  java.util.List<? extends com.google.api.LabelDescriptorOrBuilder> 
      getLabelsOrBuilderList();
  /**
   * <code>repeated .google.api.LabelDescriptor labels = 2;</code>
   *
   * <pre>
   * The set of labels that are available to describe a specific log entry.
   * Runtime requests that contain labels not specified here are
   * considered invalid.
   * </pre>
   */
  com.google.api.LabelDescriptorOrBuilder getLabelsOrBuilder(
      int index);

  /**
   * <code>optional string description = 3;</code>
   *
   * <pre>
   * A human-readable description of this log. This information appears in
   * the documentation and can contain details.
   * </pre>
   */
  java.lang.String getDescription();
  /**
   * <code>optional string description = 3;</code>
   *
   * <pre>
   * A human-readable description of this log. This information appears in
   * the documentation and can contain details.
   * </pre>
   */
  com.google.protobuf.ByteString
      getDescriptionBytes();

  /**
   * <code>optional string display_name = 4;</code>
   *
   * <pre>
   * The human-readable name for this log. This information appears on
   * the user interface and should be concise.
   * </pre>
   */
  java.lang.String getDisplayName();
  /**
   * <code>optional string display_name = 4;</code>
   *
   * <pre>
   * The human-readable name for this log. This information appears on
   * the user interface and should be concise.
   * </pre>
   */
  com.google.protobuf.ByteString
      getDisplayNameBytes();
}