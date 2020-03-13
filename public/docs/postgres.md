# PostgreSQL

**Database changes required for implementation will be done via Liquibase (see [liqubase.md](./liquibase.md) file). The following information is for development use only.**

## Creating the Schema

During development, to create the required user, empty database, and schema, the following SQL may be used. Other changes should be done with Liqubase.

```sql
create user jncc with password '{PASSWORD}';
create database jncc;
```

Connect to the new database;

```sql
create schema authorization jncc;
grant all on schema jncc to jncc;
grant all on all tables in schema jncc to jncc;
```

## Importing ESRI Shapefile with ogr2ogr

An ESRI shapefile is used to import data into the Oil and Gas Blocks table during development. For the purposes of implementation, the resulting Oil and Gas Block data will be provided for import via Liquibase (see [liqubase.md](./liquibase.md) file).

We recommend the use of the `ogr2ogr` command (part of the `gdal`) which can be aquired through the package manager on linux machines or via [OSGeo4W](https://www.osgeo.org/projects/osgeo4w/).

The shapefile is imported temporarily into the sourceblocks table using the following command line:

	ogr2ogr -f "PostgreSQL" PG:"host=host_name user=user_name dbname=jncc" shapefile.shp -nln sourceblocks -skipfailures -a_srs "EPSG:4326"

The sourceblocks table will then include 8 fields: 

	ogc_fid serial NOT NULL,
	wkb_geometry geometry(Polygon,4326),
	new_block character(10),
	lessthan_5 character(254),
	tw_code character(254),
	split_bloc character(254),
	quad character(50),
	point_req character(50),
	asign_bloc character(50)

Then use SQL to populate the oilandgasblocks table:

```sql
INSERT INTO jncc.oilandgasblock(
	id, block_code, lessthan_five, tw_code, split_block, 
	quadrant, point_req, assignment_block_code, geom)
SELECT 
ogc_fid, 
new_block, 
case when lessthan_5='yes' then true else false end, 
tw_code, 
case when split_bloc='yes' then true else false end, 
quad, 
case when point_req='yes' then true else false end, 
case when asign_bloc='n/a' then null else asign_bloc end, wkb_geometry 
FROM jncc.sourceblocks;
```

Previously more data was available and the following queries were used. These are now defunct but kept for reference.

1. Original
```sql
INSERT INTO jncc.oilandgasblock(
            action, assignment_block_id, block, block_1, block_name, 
            block_no, block_part, combined_c, comments, feature_co, five_percent, 
            label, licence, new_block, note1, note2, note3, objectid, q_b, 
            q_b_1, quad, quadrant, round, shape_area, shape_leng, split, 
            split_bloc, tw_code, water_type, geom)
SELECT action, null, block, block_1, block_name,
		block_no, block_part, combined_c, comments, feature_co, null,
		null, licence, new_block, note1, note2, note3, objectid, q_b,
		q_b_1, quad, quadrant, round, shape_area, shape_leng, null,   
		split_bloc, tw_code, null, wkb_geometry
FROM jncc.sourceblocks;
```

2. Second import
```sql
INSERT INTO jncc.oilandgasblock(
            action, assignment_block_id, block, block_1, block_name, 
            block_no, block_part, combined_c, comments, feature_co, five_percent, 
            label, licence, new_block, note1, note2, note3, objectid, q_b, 
            q_b_1, quad, quadrant, round, shape_area, shape_leng, split, 
            split_bloc, tw_code, water_type, geom)
SELECT null, null, block, null, null,
		block_no, block_part, null, null, null, case when lessthan_5='yes' then true else false end,
		null, null, new_block, note1, null, null, objectid, null,
		null, null, quadrant, null, null, null, null,   
		split_bloc, tw_code, null, wkb_geometry
FROM jncc.sourceblocks;
```

## Testing Spatial Queries

```sql
SELECT id, action, assignment_block_id, block, block_1, block_name, 
       block_no, block_part, combined_c, comments, feature_co, five_percent, 
       ST_askml(geom), label, licence, new_block, note1, note2, note3, objectid, 
       q_b, q_b_1, quad, quadrant, round, shape_area, shape_leng, split, 
       split_bloc, tw_code, water_type
  FROM jncc.oilandgasblock
  where ST_intersects(geom, ST_GeomFromText('POINT(-3.401556965659609 54.13251478413099)', 4326));

//Inside:  	ST_GeomFromText('POINT(-3.401556965659605 54.13251478413095)', 4326)
//on boundary:	ST_GeomFromText('POINT(-3.401556965659609 54.13251478413099)', 4326)
```