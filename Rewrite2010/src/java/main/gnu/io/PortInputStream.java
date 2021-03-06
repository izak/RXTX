/*-------------------------------------------------------------------------
|   RXTX License v 2.1 - LGPL v 2.1 + Linking Over Controlled Interface.
|   RXTX is a native interface to serial ports in java.
|   Copyright 1997-2009 by Trent Jarvi tjarvi@qbang.org and others who
|   actually wrote it.  See individual source files for more information.
|
|   A copy of the LGPL v 2.1 may be found at
|   http://www.gnu.org/licenses/lgpl.txt on March 4th 2007.  A copy is
|   here for your convenience.
|
|   This library is free software; you can redistribute it and/or
|   modify it under the terms of the GNU Lesser General Public
|   License as published by the Free Software Foundation; either
|   version 2.1 of the License, or (at your option) any later version.
|
|   This library is distributed in the hope that it will be useful,
|   but WITHOUT ANY WARRANTY; without even the implied warranty of
|   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
|   Lesser General Public License for more details.
|
|   An executable that contains no derivative of any portion of RXTX, but
|   is designed to work with RXTX by being dynamically linked with it,
|   is considered a "work that uses the Library" subject to the terms and
|   conditions of the GNU Lesser General Public License.
|
|   The following has been added to the RXTX License to remove
|   any confusion about linking to RXTX.   We want to allow in part what
|   section 5, paragraph 2 of the LGPL does not permit in the special
|   case of linking over a controlled interface.  The intent is to add a
|   Java Specification Request or standards body defined interface in the 
|   future as another exception but one is not currently available.
|
|   http://www.fsf.org/licenses/gpl-faq.html#LinkingOverControlledInterface
|
|   As a special exception, the copyright holders of RXTX give you
|   permission to link RXTX with independent modules that communicate with
|   RXTX solely through the Sun Microsytems CommAPI interface version 2,
|   regardless of the license terms of these independent modules, and to copy
|   and distribute the resulting combined work under terms of your choice,
|   provided that every copy of the combined work is accompanied by a complete
|   copy of the source code of RXTX (the version of RXTX used to produce the
|   combined work), being distributed under the terms of the GNU Lesser General
|   Public License plus this exception.  An independent module is a
|   module which is not derived from or based on RXTX.
|
|   Note that people who make modified versions of RXTX are not obligated
|   to grant this special exception for their modified versions; it is
|   their choice whether to do so.  The GNU Lesser General Public License
|   gives permission to release a modified version without this exception; this
|   exception also makes it possible to release a modified version which
|   carries forward this exception.
|
|   You should have received a copy of the GNU Lesser General Public
|   License along with this library; if not, write to the Free
|   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
|   All trademarks belong to their respective owners.
--------------------------------------------------------------------------*/
package javax.comm;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.InputStream;

/**
 * A port <code>InputStream</code> object.
 * <p>
 * Note that the read behavior of the input stream is controlled by settings in the
 * <code>CommPort</code> instance - see {@link javax.comm.CommPort#getInputStream()}.
 * </p>
 * <p>
 * <strong>This implementation is not synchronized.</strong> If multiple threads access a
 * <code>PortInputStream</code> instance concurrently, it <i>must</i> be synchronized
 * externally.
 * </p>
 * 
 * @author <a href="http://www.rxtx.org">The RXTX Project</a>
 */
public class PortInputStream extends InputStream {
    private static final String source = PortInputStream.class.getName();

    private final byte[] buffer;
    private boolean closed = false;
    private int index;
    private int length;
    private final CommPort port;

    public PortInputStream(CommPort port) {
        this.port = port;
        this.buffer = new byte[port.getInputBufferSize()];
        this.index = buffer.length;
        this.length = buffer.length;
    }

    public int available() throws IOException {
        return this.length - this.index;
    }

    public void close() throws IOException {
        this.closed = true;
    }

    public int read() throws IOException {
        if (this.closed == true) {
            throw new IOException("InputStream is closed");
        }
        if (this.index == this.length) {
            int framingStart = 0;
            long startTime = System.currentTimeMillis();
            int offset = this.port.readBytes(this.buffer, 0, this.buffer.length);
            while (offset < this.buffer.length) {
                if (Thread.interrupted()) {
                    InterruptedIOException exception = new InterruptedIOException();
                    exception.bytesTransferred = offset;
                    throw exception;
                }
                if (this.port.isReceiveTimeoutEnabled() && startTime + this.port.getReceiveTimeout() >= System.currentTimeMillis()) {
                    break;
                }
                if (this.port.isReceiveThresholdEnabled() && offset >= this.port.getReceiveThreshold()) {
                    break;
                }
                if (this.port.isReceiveFramingEnabled() && offset > 0) {
                    byte framingByte = (byte) this.port.getReceiveFramingByte();
                    boolean framingByteFound = false;
                    for (int i = framingStart; i <= offset; i++) {
                        if (this.buffer[i] == framingByte) {
                            framingByteFound = true;
                            break;
                        }
                    }
                    if (framingByteFound) {
                        break;
                    }
                    framingStart = offset;
                }
                int bytesRead = this.port.readBytes(this.buffer, offset, this.buffer.length - offset);
                offset += bytesRead;
            }
            this.length = offset;
            this.index = 0;
        }
        return ((int) this.buffer[this.index++]) &  0x000F;
    }
}
