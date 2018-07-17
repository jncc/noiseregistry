import static org.junit.Assert.*;
import models.OilAndGasBlock;
import models.ActivityLocation;
import models.ActivityApplication;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.test.FakeApplication;
import play.test.Helpers;
import scala.Option;
import static org.mockito.Mockito.*;

public class ProposedActivityTest {

	@PersistenceContext
	private static EntityManager em;
	
	private static ActivityApplication aa;
	
	@BeforeClass
	public static void setUp() {
		final FakeApplication app = Helpers.fakeApplication();
		Helpers.start(app);
		Option<JPAPlugin> jpaPlugin = app.getWrappedApplication().plugin(JPAPlugin.class);
		em = jpaPlugin.get().em("default");
		JPA.bindForCurrentThread(em);
		em.getTransaction().begin();
		
		aa = new ActivityApplication();
	}
	
	@AfterClass
	public static void tearDown()
	{
		em.getTransaction().rollback();
		JPA.bindForCurrentThread(null);
		if (em!=null)
			em.close();
	}
	
	@Test
	public void test_ValidOGBlock() {
		@SuppressWarnings("unchecked")
		List<OilAndGasBlock> mockedList = mock(List.class);
		OilAndGasBlock ogb = new OilAndGasBlock();
		ogb.setId(0L);
		ogb.setBlock_code("block0");
		when(mockedList.get(0)).thenReturn(ogb);
		
		OilAndGasBlock ogbTest = mockedList.get(0);
		assertEquals("block0", ogbTest.getBlock_code());
		verify(mockedList).get(0);
	}
	
	@Test
	public void test_InvalidOGBlock() {
		@SuppressWarnings("unchecked")
		List<OilAndGasBlock> mockedList = mock(List.class);
		OilAndGasBlock ogb = new OilAndGasBlock();
		ogb.setId(0L);
		ogb.setBlock_code("block0");
		when(mockedList.get(0)).thenReturn(ogb);
		
		OilAndGasBlock ogbTest = mockedList.get(1);
		assertNull(ogbTest);
		verify(mockedList).get(1);
	}
	
	@Test
	public void findBlockByCode() {
		//Initial 
    	List<OilAndGasBlock> blocks = OilAndGasBlock.findByCode("test");
	    Logger.debug("Count of blocks: " + Integer.toString(blocks.size()));
	    assertEquals(0, blocks.size());
	    
	    OilAndGasBlock ogb2 = new OilAndGasBlock();
	    ogb2.setBlock_code("test");
	    //remaining items added to allow persistence with the (new) not-null values... 
	    ogb2.setLessthan_five(false);
	    ogb2.setSplit_block(false);
	    ogb2.setTw_code("T");
	    ogb2.setQuadrant("1");
	    ogb2.setPoint_req(false);
	    
	    em.persist(ogb2);
	    Logger.debug(ogb2.toString());
	    Logger.debug("Wrote item with id: " + Long.toString(ogb2.getId()));
	    em.flush();
		
	    List<OilAndGasBlock> blocks2 = OilAndGasBlock.findByCode("test");
	    Logger.debug("Count of blocks: " + Integer.toString(blocks2.size()));
	    assertEquals(1, blocks2.size());
	}
	
	@Test 
	public void test_ValidLatLng() {
		//fail("Todo");
		ActivityLocation al = new ActivityLocation();
		al.setLat(51.520707);
		al.setLng(-0.153809);
		al.setAa(aa);
		
	}
	
	@Test
	public void test_InvalidLatLng() {
		//fail("Todo");
	}
	
}
