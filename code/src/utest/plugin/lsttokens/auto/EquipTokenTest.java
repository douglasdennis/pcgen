/*
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
package plugin.lsttokens.auto;

import org.junit.Test;

import pcgen.cdom.base.CDOMReference;
import pcgen.cdom.base.ChooseSelectionActor;
import pcgen.cdom.enumeration.ListKey;
import pcgen.cdom.reference.CDOMGroupRef;
import pcgen.cdom.reference.CDOMSingleRef;
import pcgen.core.Equipment;
import pcgen.core.QualifiedObject;
import pcgen.persistence.PersistenceLayerException;
import pcgen.rules.persistence.token.CDOMSecondaryToken;
import plugin.lsttokens.testsupport.AbstractAutoTokenTestCase;
import plugin.lsttokens.testsupport.ConsolidationRule;

public class EquipTokenTest extends AbstractAutoTokenTestCase<Equipment>
{

	static EquipToken subtoken = new EquipToken();

	@Override
	protected ConsolidationRule getConsolidationRule()
	{
		return new ConsolidationRule()
		{
            @Override
			public String[] getAnswer(String... strings)
			{
				return new String[] { "EQUIP|TestWP1|TestWP1|TestWP2|TestWP2|TestWP3" };
			}
		};
	}

	@Override
	public CDOMSecondaryToken<?> getSubToken()
	{
		return subtoken;
	}

	@Override
	public Class<Equipment> getTargetClass()
	{
		return Equipment.class;
	}

	@Override
	public boolean isAllLegal()
	{
		return false;
	}

	@Test
	public void testEmpty()
	{
		// Just to get Eclipse to recognize this as a JUnit 4.0 Test Case
	}

	@Override
	protected ChooseSelectionActor<Equipment> getActor()
	{
		return subtoken;
	}

	@Override
	protected void loadAllReference()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected void loadProf(CDOMSingleRef<Equipment> ref)
	{
		primaryProf.addToListFor(ListKey.EQUIPMENT,
				new QualifiedObject<CDOMReference<Equipment>>(ref));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUnparseGenericsFail() throws PersistenceLayerException
	{
		ListKey listKey = ListKey.EQUIPMENT;
		primaryProf.addToListFor(listKey, new Object());
		try
		{
			getToken().unparse(primaryContext, primaryProf);
			fail();
		}
		catch (ClassCastException e)
		{
			// Yep!
		}
	}

	@Override
	protected void loadTypeProf(String... types)
	{
		CDOMGroupRef<Equipment> ref = primaryContext.getReferenceContext().getCDOMTypeReference(
				Equipment.class, types);
		primaryProf.addToListFor(ListKey.EQUIPMENT,
				new QualifiedObject<CDOMReference<Equipment>>(ref));
	}

	@Override
	protected boolean allowsPrerequisite()
	{
		return true;
	}
}
