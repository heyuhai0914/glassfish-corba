/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1994-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.rmic.tools.tree;

import org.glassfish.rmic.tools.java.*;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class Node implements Constants, Cloneable {
    int op;
    long where;

    /**
     * Constructor
     */
    Node(int op, long where) {
        this.op = op;
        this.where = where;
    }

    /**
     * Get the operator
     */
    public int getOp() {
        return op;
    }

    /**
     * Get where
     */
    public long getWhere() {
        return where;
    }

    /**
     * Implicit conversions
     */
    public Expression convert(Environment env, Context ctx, Type t, Expression e) {
        if (e.type.isType(TC_ERROR) || t.isType(TC_ERROR)) {
            // An error was already reported
            return e;
        }

        if (e.type.equals(t)) {
            // The types are already the same
            return e;
        }

        try {
            if (e.fitsType(env, ctx, t)) {
                return new ConvertExpression(where, t, e);
            }

            if (env.explicitCast(e.type, t)) {
                env.error(where, "explicit.cast.needed", opNames[op], e.type, t);
                return new ConvertExpression(where, t, e);
            }
        } catch (ClassNotFound ee) {
            env.error(where, "class.not.found", ee.name, opNames[op]);
        }

        // The cast is not allowed
        env.error(where, "incompatible.type", opNames[op], e.type, t);
        return new ConvertExpression(where, Type.tError, e);
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        throw new CompilerError("print");
    }

    /**
     * Clone this object.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw (InternalError) new InternalError().initCause(e);
        }
    }

    /*
     * Useful for simple debugging
     */
    public String toString() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        print(new PrintStream(bos));
        return bos.toString();
    }

}
