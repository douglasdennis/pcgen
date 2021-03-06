/*
 * Copyright (c) 2009 Tom Parker <thpr@users.sourceforge.net>
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
package plugin.lsttokens.editcontext;

import org.junit.Test;

import pcgen.cdom.base.CDOMObject;
import pcgen.cdom.list.CompanionList;
import pcgen.core.Ability;
import pcgen.persistence.PersistenceLayerException;
import pcgen.rules.persistence.CDOMLoader;
import pcgen.rules.persistence.token.CDOMPrimaryToken;
import plugin.lsttokens.FollowersLst;
import plugin.lsttokens.editcontext.testsupport.AbstractIntegrationTestCase;
import plugin.lsttokens.editcontext.testsupport.TestContext;
import plugin.lsttokens.testsupport.CDOMTokenLoader;

public class FollowersIntegrationTest extends
		AbstractIntegrationTestCase<CDOMObject>
{
	static FollowersLst token = new FollowersLst();
	static CDOMTokenLoader<CDOMObject> loader = new CDOMTokenLoader<CDOMObject>();

	@Override
	public Class<Ability> getCDOMClass()
	{
		return Ability.class;
	}

	@Override
	public CDOMLoader<CDOMObject> getLoader()
	{
		return loader;
	}

	@Override
	public CDOMPrimaryToken<CDOMObject> getToken()
	{
		return token;
	}

	@Test
	public void testRoundRobinSimple() throws PersistenceLayerException
	{
		verifyCleanStart();
		primaryContext.getReferenceContext().constructCDOMObject(CompanionList.class, "Follower");
		secondaryContext.getReferenceContext().constructCDOMObject(CompanionList.class,
				"Follower");
		TestContext tc = new TestContext();
		commit(testCampaign, tc, "Follower|4+1");
		commit(modCampaign, tc, "Follower|Formula1");
		completeRoundRobin(tc);
	}

	@Test
	public void testRoundRobinRemove() throws PersistenceLayerException
	{
		verifyCleanStart();
		primaryContext.getReferenceContext().constructCDOMObject(CompanionList.class, "Follower");
		secondaryContext.getReferenceContext().constructCDOMObject(CompanionList.class,
				"Follower");
		primaryContext.getReferenceContext().constructCDOMObject(CompanionList.class, "Pet");
		secondaryContext.getReferenceContext().constructCDOMObject(CompanionList.class, "Pet");
		TestContext tc = new TestContext();
		commit(testCampaign, tc, "Follower|4+1");
		commit(modCampaign, tc, "Pet|PetForm");
		completeRoundRobin(tc);
	}

	@Test
	public void testRoundRobinNoSet() throws PersistenceLayerException
	{
		verifyCleanStart();
		primaryContext.getReferenceContext().constructCDOMObject(CompanionList.class, "Follower");
		secondaryContext.getReferenceContext().constructCDOMObject(CompanionList.class,
				"Follower");
		TestContext tc = new TestContext();
		emptyCommit(testCampaign, tc);
		commit(modCampaign, tc, "Follower|4+1");
		completeRoundRobin(tc);
	}

	@Test
	public void testRoundRobinNoReset() throws PersistenceLayerException
	{
		verifyCleanStart();
		primaryContext.getReferenceContext().constructCDOMObject(CompanionList.class, "Follower");
		secondaryContext.getReferenceContext().constructCDOMObject(CompanionList.class,
				"Follower");
		TestContext tc = new TestContext();
		commit(testCampaign, tc, "Follower|4+1");
		emptyCommit(modCampaign, tc);
		completeRoundRobin(tc);
	}

}
