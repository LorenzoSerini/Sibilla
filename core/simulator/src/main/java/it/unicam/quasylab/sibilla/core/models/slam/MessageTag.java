/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.core.models.slam;

/**
 * The class <code>MessageTag</code> represents tags associated with messages.
 */
public class MessageTag {

    private final String tagName;
    private final SlamType[] args;
    private final int tagId;

    /**
     * Creates a new <code>MessageTag</code> with the given id and name. We have to guarnatee that
     * for any pair <code>m1</code> and <code>m2</code> of instances of of <code>MessageTag</code> we have that:
     * <code>m1.tagId==m2.tagId</code> if and only if <code>m1.tagName.equals(m2.tagName)</code>.
     *  @param tagId tag identifier;
     * @param tagName tag name.
     * @param args
     */
    public MessageTag(int tagId, String tagName, SlamType[] args) {
        this.tagName = tagName;
        this.tagId = tagId;
        this.args = args;
    }

    /**
     * Returns message tag name.
     *
     * @return message tag name.
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * Returns message tag id.
     *
     * @return message tag id.
     */
    public int getTagId() {
        return tagId;
    }

    /**
     * Returns the number of values associated with messages of this tag.
     *
     * @return the number of values associated with messages of  this tag.
     */
    public int getArity() { return args.length; }

    /**
     * Returns the data type of values in position <code>idx</code> of messages
     * with this tag.
     *
     * @param idx an message index.
     * @return the data type of values in position <code>idx</code> of messages
     * with this tag.
     */
    public SlamType getTypeOf(int idx) { return this.args[idx]; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageTag that = (MessageTag) o;
        return getTagId() == that.getTagId();
    }

    @Override
    public int hashCode() {
        return getTagId();
    }

    @Override
    public String toString() {
        return tagName;
    }
}
