/*
 * Copyright (c) 2008 Tom Parker <thpr@users.sourceforge.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */
package pcgen.base.lang;

import org.junit.Test;

import junit.framework.TestCase;

public class UnreachableErrorTest extends TestCase {

	@Test
	public void testEmptyConstructor()
	{
		UnreachableError unreachableError = new UnreachableError();
		assertNotNull(unreachableError);
	}
	
	@Test
	public void testMessageConstructor()
	{
		String expectedResult = "Foobar";
		UnreachableError unreachableError = new UnreachableError("Foobar");
		String result = unreachableError.getMessage();
		assertTrue(result.equals(expectedResult));
	}

	@Test
	public void testCauseConstructor()
	{
		UnreachableError unreachableError = new UnreachableError(new NullPointerException());
		Throwable result = unreachableError.getCause();
		assertTrue(result instanceof NullPointerException);
	}

	@Test
	public void testMessageAndCauseConstructor()
	{
		String expectedResult = "Foobar";
		UnreachableError unreachableError = new UnreachableError("Foobar", new NullPointerException());
		String result = unreachableError.getMessage();
		Throwable result2 = unreachableError.getCause();
		assertTrue(result.equals(expectedResult));
		assertTrue(result2 instanceof NullPointerException);
	}

}

