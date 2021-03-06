/**
 * Appia: Group communication and protocol composition framework library
 * Copyright 2006 University of Lisbon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *
 * Initial developer(s): Alexandre Pinto and Hugo Miranda.
 * Contributor(s): See Appia web page for a list of contributors.
 */
 /*
 * FragTimer.java
 *
 * Created on January 31, 2003, 10:55 AM
 */

package net.sf.appia.protocols.frag;

import net.sf.appia.core.*;
import net.sf.appia.core.events.channel.PeriodicTimer;


/**
 *
 * @author Alexandre Pinto
 */
public class FragTimer extends PeriodicTimer {
  
  /** Creates a new instance of FragTimer */
  public FragTimer() {}

  public FragTimer(long period, Channel channel, Session source, int qualifier) 
  throws AppiaEventException, AppiaException {
    super("FragSession: "+source,period,channel,Direction.DOWN,source,qualifier);
  }
}
