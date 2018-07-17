package models;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Point;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

import play.Logger;
import play.db.jpa.JPA;


@NamedNativeQueries({
	@NamedNativeQuery(name = "OilAndGasBlock.findBlocksByCode", query = "select * from oilandgasblock ogb where (ogb.block_code = ?)" , resultClass = OilAndGasBlock.class),
	@NamedNativeQuery(name = "OilAndGasBlock.findBlocksByLngLat", query = "select * from oilandgasblock ogb where (ST_intersects(ogb.geom, ST_SetSRID(ST_POINT(:lng, :lat), 4326)))" , resultClass = OilAndGasBlock.class),
	@NamedNativeQuery(name = "OilAndGasBlock.findBlocksByPoint", query = "select * from oilandgasblock ogb where (ST_intersects(ogb.geom, ?))" , resultClass = OilAndGasBlock.class),
	@NamedNativeQuery(name = "OilAndGasBlock.findBlocksByPolygon", query = "select * from oilandgasblock ogb where (ST_intersects(ogb.geom, ?))" , resultClass = OilAndGasBlock.class)
})

@Entity
@Table(name="oilandgasblock")
public class OilAndGasBlock 
{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "oilandgasblock_seq_gen")
    @SequenceGenerator(name = "oilandgasblock_seq_gen", sequenceName = "oilandgasblock_id_seq")
    @Column(columnDefinition = "SERIAL")
    protected Long id;
    
    @Column(length=6)
    protected String block_code;
    
    @Column(columnDefinition = "BIT", length = 1)
    protected Boolean lessthan_five;
    
    @Column(length=1)
    protected String tw_code;
    
    @Column(columnDefinition = "BIT", length = 1)
    protected Boolean split_block;

    @Column(length=3)
    protected String quadrant;
    
    @Column(columnDefinition = "BIT", length = 1)
    protected Boolean point_req;
    
    @Column(length=6)
    String assignment_block_code;
     
    @Type(type = "org.hibernate.spatial.GeometryType")
    protected Polygon geom;
        
    /**
     * Gets the code
     * @return
     */
	public String getBlock_code() {
		return block_code;
	}
	/**
	 * Sets the code
	 * @param block_code
	 */
	public void setBlock_code(String block_code) {
		this.block_code = block_code;
	}
	/**
	 * Gets the "Less than five percent"
	 * @return
	 */
	public Boolean getLessthan_five() {
		return lessthan_five;
	}
	/**
	 * Sets "Less than 5%"
	 * @param lessthan_five
	 */
	public void setLessthan_five(Boolean lessthan_five) {
		this.lessthan_five = lessthan_five;
	}
	/**
	 * Gets the TW code 
	 * @return
	 */
	public String getTw_code() {
		return tw_code;
	}
	/**
	 * Sets the TW code
	 * @param tw_code
	 */
	public void setTw_code(String tw_code) {
		this.tw_code = tw_code;
	}
	/**
	 * Gets whether this is a split block
	 * @return
	 */
	public Boolean getSplit_block() {
		return split_block;
	}
	/**
	 * Sets whether this is a split block
	 * @param split_block
	 */
	public void setSplit_block(Boolean split_block) {
		this.split_block = split_block;
	}
	/**
	 * Gets the quadrant 
	 * @return
	 */
	public String getQuadrant() {
		return quadrant;
	}
	/**
	 * Sets the quadrant
	 * @param quadrant
	 */
	public void setQuadrant(String quadrant) {
		this.quadrant = quadrant;
	}
	/**
	 * Gets the point required
	 * @return
	 */
	public Boolean getPoint_req() {
		return point_req;
	}
	/**
	 * Sets the point required
	 * @param point_req
	 */
	public void setPoint_req(Boolean point_req) {
		this.point_req = point_req;
	}
	/**
	 * Gets the assignment block code 
	 * @return
	 */
	public String getAssignment_block_code() {
		return assignment_block_code;
	}
	/**
	 * Sets the assignment block code
	 * @param assignment_block_code
	 */
	public void setAssignment_block_code(String assignment_block_code) {
		this.assignment_block_code = assignment_block_code;
	}
	/**
	 * Gets the polygon
	 * @return
	 */
	public Polygon getGeom() {
		return geom;
	}
	/**
	 * Sets the polygon
	 * @param geom
	 */
	public void setGeom(Polygon geom) {
		this.geom = geom;
	}
	/**
	 * Gets the id
	 * @return
	 */
	public Long getId() {
		return id;
	}
	/**
	 * Sets the id
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}
	    
	/**
	 * @param code
	 * @return a list of blocks found with the supplied code
	 */
	public static List<OilAndGasBlock> findByCode(String code) {
		try
		{
			TypedQuery<OilAndGasBlock> query = JPA.em().createNamedQuery("OilAndGasBlock.findBlocksByCode", OilAndGasBlock.class);
			query.setParameter(1, code);
			List<OilAndGasBlock> blocks = query.getResultList();
			return blocks;
		} catch (Exception e)
		{
			Logger.info("Error finding OGB by code: "+e.toString());
			return new ArrayList<OilAndGasBlock>();
		}
	}
	
	/**
	 * @param point
	 * @return a list of blocks found intersecting with the supplied point
	 */
	public static List<OilAndGasBlock> findByPoint(Point point) {
		TypedQuery<OilAndGasBlock> query = JPA.em().createNamedQuery("OilAndGasBlock.findBlocksByPoint", OilAndGasBlock.class);
		query.setParameter(1, point);
		List<OilAndGasBlock> blocks = query.getResultList();
		return blocks;
	}
	
	/**
	 * @param polygon
	 * @return a list of blocks found intersecting with the supplied polygon
	 */
	public static List<OilAndGasBlock> findByPolygon(Polygon polygon) {
		TypedQuery<OilAndGasBlock> query = JPA.em().createNamedQuery("OilAndGasBlock.findBlocksByPolygon", OilAndGasBlock.class);
		query.setParameter(1, polygon);
		List<OilAndGasBlock> blocks = query.getResultList();
		return blocks;
	}
	
	/**
	 * @param String lat
	 * @param String lng
	 * @return a list of blocks found intersecting with the supplied lat/lng coordinates
	 */
	public static List<OilAndGasBlock> findByLngLat(Double lng, Double lat) {
		TypedQuery<OilAndGasBlock> query = JPA.em().createNamedQuery("OilAndGasBlock.findBlocksByLngLat", OilAndGasBlock.class);
		
		query.setParameter("lat", lat);
		query.setParameter("lng", lng);

		List<OilAndGasBlock> blocks = query.getResultList();
		return blocks;
	}
}