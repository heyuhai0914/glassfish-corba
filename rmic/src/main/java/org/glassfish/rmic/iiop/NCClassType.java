/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1998-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package org.glassfish.rmic.iiop;

import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassNotFound;
import org.glassfish.rmic.tools.java.CompilerError;

import java.util.Vector;

/**
 * NCClassType represents any non-special class which does not
 * extends one or more interfaces which inherit from java.rmi.Remote.
 * <p>
 * The static forImplementation(...) method must be used to obtain an instance,
 * and will return null if the ClassDefinition is non-conforming.
 *
 * @author      Bryan Atsatt
 */
public class NCClassType extends ClassType {

    //_____________________________________________________________________
    // Public Interfaces
    //_____________________________________________________________________

    /**
     * Create an NCClassType for the given class.
     *
     * If the class is not a properly formed or if some other error occurs, the
     * return value will be null, and errors will have been reported to the
     * supplied BatchEnvironment.
     */
    public static NCClassType forNCClass(ClassDefinition classDef,
                                         ContextStack stack) {

        if (stack.anyErrors()) return null;

        boolean doPop = false;
        try {
            // Do we already have it?

            org.glassfish.rmic.tools.java.Type theType = classDef.getType();
            Type existing = getType(theType,stack);

            if (existing != null) {

                if (!(existing instanceof NCClassType)) return null; // False hit.

                                // Yep, so return it...

                return (NCClassType) existing;

            }

            NCClassType it = new NCClassType(stack, classDef);
            putType(theType,it,stack);
            stack.push(it);
            doPop = true;

            if (it.initialize(stack)) {
                stack.pop(true);
                return it;
            } else {
                removeType(theType,stack);
                stack.pop(false);
                return null;
            }
        } catch (CompilerError e) {
            if (doPop) stack.pop(false);
            return null;
        }
    }

    /**
     * Return a string describing this type.
     */
    public String getTypeDescription () {
        return addExceptionDescription("Non-conforming class");
    }

    //_____________________________________________________________________
    // Internal/Subclass Interfaces
    //_____________________________________________________________________

    /**
     * Create a NCClassType instance for the given class.  The resulting
     * object is not yet completely initialized.
     */
    private NCClassType(ContextStack stack, ClassDefinition classDef) {
        super(stack,classDef,TYPE_NC_CLASS | TM_CLASS | TM_COMPOUND);
    }

    //_____________________________________________________________________
    // Internal Interfaces
    //_____________________________________________________________________

    /**
     * Initialize this instance.
     */
    private boolean initialize (ContextStack stack) {
        if (!initParents(stack)) {
            return false;
        }

        if (stack.getEnv().getParseNonConforming()) {

            Vector directInterfaces = new Vector();
            Vector directMethods = new Vector();
            Vector directMembers = new Vector();

            try {

                // Get methods...

                if (addAllMethods(getClassDefinition(),directMethods,false,false,stack) != null) {

                    // Update parent class methods...

                    if (updateParentClassMethods(getClassDefinition(),directMethods,false,stack) != null) {

                    // Get conforming constants...

                    if (addConformingConstants(directMembers,false,stack)) {

                        // We're ok, so pass 'em up...

                        if (!initialize(directInterfaces,directMethods,directMembers,stack,false)) {
                            return false;
                        }
                    }
                    }
                }
                return true;

            } catch (ClassNotFound e) {
                classNotFound(stack,e);
            }
            return false;
        } else {
            return initialize(null,null,null,stack,false);
        }
    }
}
