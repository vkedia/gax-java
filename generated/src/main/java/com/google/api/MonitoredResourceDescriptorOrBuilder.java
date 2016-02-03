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
// source: google/api/monitored_resource.proto

package com.google.api;

public interface MonitoredResourceDescriptorOrBuilder extends
    // @@protoc_insertion_point(interface_extends:google.api.MonitoredResourceDescriptor)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional string type = 1;</code>
   *
   * <pre>
   * The monitored resource type. For example, the type `"cloudsql_database"`
   * represents databases in Google Cloud SQL.
   * </pre>
   */
  java.lang.String getType();
  /**
   * <code>optional string type = 1;</code>
   *
   * <pre>
   * The monitored resource type. For example, the type `"cloudsql_database"`
   * represents databases in Google Cloud SQL.
   * </pre>
   */
  com.google.protobuf.ByteString
      getTypeBytes();

  /**
   * <code>optional string display_name = 2;</code>
   *
   * <pre>
   * A concise name for the monitored resource type, which is displayed in user
   * interfaces. For example, `"Cloud SQL database"`.
   * </pre>
   */
  java.lang.String getDisplayName();
  /**
   * <code>optional string display_name = 2;</code>
   *
   * <pre>
   * A concise name for the monitored resource type, which is displayed in user
   * interfaces. For example, `"Cloud SQL database"`.
   * </pre>
   */
  com.google.protobuf.ByteString
      getDisplayNameBytes();

  /**
   * <code>optional string description = 3;</code>
   *
   * <pre>
   * A detailed description of the monitored resource type, which is used in
   * documentation.
   * </pre>
   */
  java.lang.String getDescription();
  /**
   * <code>optional string description = 3;</code>
   *
   * <pre>
   * A detailed description of the monitored resource type, which is used in
   * documentation.
   * </pre>
   */
  com.google.protobuf.ByteString
      getDescriptionBytes();

  /**
   * <code>repeated .google.api.LabelDescriptor labels = 4;</code>
   *
   * <pre>
   * A set of labels that can be used to describe instances of this monitored
   * resource type. For example, Cloud SQL databases can be labeled with
   * their `"database_id"` and their `"zone"`.
   * </pre>
   */
  java.util.List<com.google.api.LabelDescriptor> 
      getLabelsList();
  /**
   * <code>repeated .google.api.LabelDescriptor labels = 4;</code>
   *
   * <pre>
   * A set of labels that can be used to describe instances of this monitored
   * resource type. For example, Cloud SQL databases can be labeled with
   * their `"database_id"` and their `"zone"`.
   * </pre>
   */
  com.google.api.LabelDescriptor getLabels(int index);
  /**
   * <code>repeated .google.api.LabelDescriptor labels = 4;</code>
   *
   * <pre>
   * A set of labels that can be used to describe instances of this monitored
   * resource type. For example, Cloud SQL databases can be labeled with
   * their `"database_id"` and their `"zone"`.
   * </pre>
   */
  int getLabelsCount();
  /**
   * <code>repeated .google.api.LabelDescriptor labels = 4;</code>
   *
   * <pre>
   * A set of labels that can be used to describe instances of this monitored
   * resource type. For example, Cloud SQL databases can be labeled with
   * their `"database_id"` and their `"zone"`.
   * </pre>
   */
  java.util.List<? extends com.google.api.LabelDescriptorOrBuilder> 
      getLabelsOrBuilderList();
  /**
   * <code>repeated .google.api.LabelDescriptor labels = 4;</code>
   *
   * <pre>
   * A set of labels that can be used to describe instances of this monitored
   * resource type. For example, Cloud SQL databases can be labeled with
   * their `"database_id"` and their `"zone"`.
   * </pre>
   */
  com.google.api.LabelDescriptorOrBuilder getLabelsOrBuilder(
      int index);
}