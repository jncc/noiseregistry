--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Enable POSTGIS Extensions
--

CREATE EXTENSION postigs;

--
-- Table: public.oilandgasblock
--

CREATE TABLE public.oilandgasblock
(
    id integer NOT NULL DEFAULT nextval('oilandgasblock_id_seq'::regclass),
    block_code character varying(10) COLLATE pg_catalog."default" NOT NULL,
    lessthan_five boolean NOT NULL,
    split_block boolean NOT NULL,
    tw_code character varying(1) COLLATE pg_catalog."default" NOT NULL,
    quadrant character varying(3) COLLATE pg_catalog."default" NOT NULL,
    point_req boolean NOT NULL,
    assignment_block_code character varying(10) COLLATE pg_catalog."default",
    geom geometry(Polygon,4326)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.oilandgasblock
    OWNER to postgres;

--
-- TOC entry 9 (class 2615 OID 19713)
-- Name: jncc; Type: SCHEMA; Schema: -; Owner: jncc
--

CREATE SCHEMA jncc;


ALTER SCHEMA jncc OWNER TO jncc;

--
-- TOC entry 1904 (class 1247 OID 19761)
-- Name: activitytypes; Type: TYPE; Schema: jncc; Owner: jncc
--

CREATE TYPE jncc.activitytypes AS ENUM (
    'Seismic Survey',
    'Geophysical Survey',
    'Piling',
    'Explosives',
    'Acoustic Deterrent Device',
    'Multibeam Echosounders'
);


ALTER TYPE jncc.activitytypes OWNER TO jncc;

SET default_tablespace = '';

--
-- TOC entry 204 (class 1259 OID 19773)
-- Name: Xuser; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc."Xuser" (
    email character varying(255) NOT NULL,
    forename character varying(255),
    fullname character varying(255)
);


ALTER TABLE jncc."Xuser" OWNER TO jncc;

--
-- TOC entry 205 (class 1259 OID 19779)
-- Name: activityacousticdd; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.activityacousticdd (
    id integer NOT NULL,
    frequency integer,
    sound_exposure_level integer,
    sound_pressure_level integer,
    activityapplication_id integer,
    frequency_actual integer,
    sound_pressure_level_actual integer,
    sound_exposure_level_actual integer
);


ALTER TABLE jncc.activityacousticdd OWNER TO jncc;

--
-- TOC entry 206 (class 1259 OID 19782)
-- Name: activityacousticdd_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.activityacousticdd_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.activityacousticdd_id_seq OWNER TO jncc;

--
-- TOC entry 3806 (class 0 OID 0)
-- Dependencies: 206
-- Name: activityacousticdd_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.activityacousticdd_id_seq OWNED BY jncc.activityacousticdd.id;


--
-- TOC entry 207 (class 1259 OID 19784)
-- Name: activityapplication; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.activityapplication (
    id integer NOT NULL,
    date_closed date,
    date_due date,
    date_end date,
    date_start date,
    date_updated timestamp with time zone,
    duration integer NOT NULL,
    status character varying(20),
    regulator_id integer NOT NULL,
    noiseproducer_id integer NOT NULL,
    activitytype_id integer,
    non_licensable boolean DEFAULT false,
    parent_id integer,
    use_parent_location boolean DEFAULT false,
    supp_info character varying(500),
    reg_name_orig character varying(100)
);


ALTER TABLE jncc.activityapplication OWNER TO jncc;

--
-- TOC entry 208 (class 1259 OID 19792)
-- Name: activityapplication_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.activityapplication_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.activityapplication_id_seq OWNER TO jncc;

--
-- TOC entry 3807 (class 0 OID 0)
-- Dependencies: 208
-- Name: activityapplication_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.activityapplication_id_seq OWNED BY jncc.activityapplication.id;


--
-- TOC entry 209 (class 1259 OID 19794)
-- Name: activityexplosives; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.activityexplosives (
    id integer NOT NULL,
    tnt_equivalent numeric,
    activityapplication_id integer,
    sound_pressure_level integer,
    sound_exposure_level integer,
    sound_pressure_level_actual integer,
    sound_exposure_level_actual integer,
    tnt_equivalent_actual numeric
);


ALTER TABLE jncc.activityexplosives OWNER TO jncc;

--
-- TOC entry 210 (class 1259 OID 19800)
-- Name: activitylocation; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.activitylocation (
    id integer NOT NULL,
    creation_type character varying(10),
    entered_ogb_code character varying(255),
    entered_point public.geometry(Point,4326),
    entered_polygon public.geometry(Polygon,4326),
    no_activity boolean,
    activityapplication_id integer,
    decomposed boolean DEFAULT false
);


ALTER TABLE jncc.activitylocation OWNER TO jncc;

--
-- TOC entry 211 (class 1259 OID 19807)
-- Name: activitymod; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.activitymod (
    id integer NOT NULL,
    source character varying(20),
    activityapplication_id integer
);


ALTER TABLE jncc.activitymod OWNER TO jncc;

--
-- TOC entry 212 (class 1259 OID 19810)
-- Name: activitymultibeames; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.activitymultibeames (
    id integer NOT NULL,
    frequency integer,
    sound_exposure_level integer,
    sound_pressure_level integer,
    activityapplication_id integer,
    frequency_actual integer,
    sound_pressure_level_actual integer,
    sound_exposure_level_actual integer
);


ALTER TABLE jncc.activitymultibeames OWNER TO jncc;

--
-- TOC entry 213 (class 1259 OID 19813)
-- Name: activitypiling; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.activitypiling (
    id integer NOT NULL,
    max_hammer_energy integer,
    activityapplication_id integer,
    sound_pressure_level integer,
    sound_exposure_level integer,
    sound_pressure_level_actual integer,
    sound_exposure_level_actual integer,
    max_hammer_energy_actual integer
);


ALTER TABLE jncc.activitypiling OWNER TO jncc;

--
-- TOC entry 214 (class 1259 OID 19816)
-- Name: activityseismic; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.activityseismic (
    id integer NOT NULL,
    data_type character varying(2),
    max_airgun_volume integer,
    other_survey_type character varying(50),
    sound_exposure_level integer,
    sound_pressure_level integer,
    survey_type character varying(10),
    activityapplication_id integer,
    max_airgun_volume_actual integer,
    sound_pressure_level_actual integer,
    sound_exposure_level_actual integer
);


ALTER TABLE jncc.activityseismic OWNER TO jncc;

--
-- TOC entry 215 (class 1259 OID 19819)
-- Name: activitysubbottomprofilers; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.activitysubbottomprofilers (
    id integer NOT NULL,
    frequency integer,
    sound_exposure_level integer,
    sound_pressure_level integer,
    source character varying(10),
    activityapplication_id integer,
    frequency_actual integer,
    sound_pressure_level_actual integer,
    sound_exposure_level_actual integer
);


ALTER TABLE jncc.activitysubbottomprofilers OWNER TO jncc;

--
-- TOC entry 216 (class 1259 OID 19822)
-- Name: activitytype; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.activitytype (
    id integer NOT NULL,
    name character varying(30)
);


ALTER TABLE jncc.activitytype OWNER TO jncc;

--
-- TOC entry 217 (class 1259 OID 19825)
-- Name: noiseproducer; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.noiseproducer (
    id integer NOT NULL,
    organisation_id integer
);


ALTER TABLE jncc.noiseproducer OWNER TO jncc;

--
-- TOC entry 218 (class 1259 OID 19828)
-- Name: oilandgasblock; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.oilandgasblock (
    id integer NOT NULL,
    block_code character varying(10) NOT NULL,
    lessthan_five boolean NOT NULL,
    split_block boolean NOT NULL,
    tw_code character varying(1) NOT NULL,
    quadrant character varying(3) NOT NULL,
    point_req boolean NOT NULL,
    assignment_block_code character varying(10),
    geom public.geometry(Polygon,4326)
);


ALTER TABLE jncc.oilandgasblock OWNER TO jncc;

--
-- TOC entry 219 (class 1259 OID 19834)
-- Name: organisation; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.organisation (
    id integer NOT NULL,
    contact_email character varying(50),
    contact_name character varying(50),
    contact_phone character varying(20),
    organisation_name character varying(100),
    administrator boolean DEFAULT false
);


ALTER TABLE jncc.organisation OWNER TO jncc;

--
-- TOC entry 220 (class 1259 OID 19838)
-- Name: regulator; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.regulator (
    id integer NOT NULL,
    closeoutdays integer,
    organisation_id integer,
    accepts_email boolean DEFAULT false
);


ALTER TABLE jncc.regulator OWNER TO jncc;

--
-- TOC entry 221 (class 1259 OID 19842)
-- Name: activityapplicationblocks; Type: VIEW; Schema: jncc; Owner: jncc
--

CREATE VIEW jncc.activityapplicationblocks AS
 SELECT block_data.aan,
    block_data.noise_producer,
    block_data.regulator,
    block_data.activity_type,
    block_data.status,
    block_data.non_licensable,
    block_data.date_start,
    block_data.date_end,
    block_data.date_due,
    block_data.date_closed,
    block_data.date_updated,
    block_data.duration,
    block_data.parent_id,
    block_data.seismic_survey_type,
    block_data.seismic_data_type,
    block_data.seismic_other_survey_type,
    block_data.seismic_max_airgun_volume,
    block_data.seismic_sound_pressure_level,
    block_data.seismic_sound_exposure_level,
    block_data.seismic_max_airgun_volume_actual,
    block_data.seismic_sound_pressure_level_actual,
    block_data.seismic_sound_exposure_level_actual,
    block_data.sbp_source,
    block_data.sbp_frequency,
    block_data.sbp_sound_pressure_level,
    block_data.sbp_sound_exposure_level,
    block_data.sbp_frequency_actual,
    block_data.sbp_sound_pressure_level_actual,
    block_data.sbp_sound_exposure_level_actual,
    block_data.piling_max_hammer_energy,
    block_data.piling_sound_pressure_level,
    block_data.piling_sound_exposure_level,
    block_data.piling_max_hammer_energy_actual,
    block_data.piling_sound_pressure_level_actual,
    block_data.piling_sound_exposure_level_actual,
    block_data.exp_tnt_equivalent,
    block_data.exp_sound_pressure_level,
    block_data.exp_sound_exposure_level,
    block_data.exp_tnt_equivalent_actual,
    block_data.exp_sound_pressure_level_actual,
    block_data.exp_sound_exposure_level_actual,
    block_data.add_frequency,
    block_data.add_sound_pressure_level,
    block_data.add_sound_exposure_level,
    block_data.add_frequency_actual,
    block_data.add_sound_pressure_level_actual,
    block_data.add_sound_exposure_level_actual,
    block_data.mbes_frequency,
    block_data.mbes_sound_pressure_level,
    block_data.mbes_sound_exposure_level,
    block_data.mbes_frequency_actual,
    block_data.mbes_sound_pressure_level_actual,
    block_data.mbes_sound_exposure_level_actual,
    block_data.actlocn_id,
    block_data.entered_ogb_code,
    block_data.point_text,
    block_data.polygon_text,
    block_data.creation_type,
    block_data.decomposed,
    block_data.no_activity,
    block_data.block_code,
    block_data.quadrant,
    block_data.lessthan_five,
    block_data.tw_code,
    block_data.split_block,
    block_data.assignment_block_code,
    block_data.point_req
   FROM ( SELECT nporg.organisation_name AS noise_producer,
            regorg.organisation_name AS regulator,
            atyp.name AS activity_type,
            aapp.id AS aan,
            aapp.status,
            aapp.non_licensable,
            aapp.date_start,
            aapp.date_end,
            aapp.date_due,
            aapp.date_closed,
            aapp.date_updated,
            aapp.duration,
            aapp.parent_id,
            aseismic.survey_type AS seismic_survey_type,
            aseismic.data_type AS seismic_data_type,
            aseismic.other_survey_type AS seismic_other_survey_type,
            aseismic.max_airgun_volume AS seismic_max_airgun_volume,
            aseismic.sound_pressure_level AS seismic_sound_pressure_level,
            aseismic.sound_exposure_level AS seismic_sound_exposure_level,
            aseismic.max_airgun_volume_actual AS seismic_max_airgun_volume_actual,
            aseismic.sound_pressure_level_actual AS seismic_sound_pressure_level_actual,
            aseismic.sound_exposure_level_actual AS seismic_sound_exposure_level_actual,
            asbp.source AS sbp_source,
            asbp.frequency AS sbp_frequency,
            asbp.sound_pressure_level AS sbp_sound_pressure_level,
            asbp.sound_exposure_level AS sbp_sound_exposure_level,
            asbp.frequency_actual AS sbp_frequency_actual,
            asbp.sound_pressure_level_actual AS sbp_sound_pressure_level_actual,
            asbp.sound_exposure_level_actual AS sbp_sound_exposure_level_actual,
            apiling.max_hammer_energy AS piling_max_hammer_energy,
            apiling.sound_pressure_level AS piling_sound_pressure_level,
            apiling.sound_exposure_level AS piling_sound_exposure_level,
            apiling.max_hammer_energy_actual AS piling_max_hammer_energy_actual,
            apiling.sound_pressure_level_actual AS piling_sound_pressure_level_actual,
            apiling.sound_exposure_level_actual AS piling_sound_exposure_level_actual,
            aexp.tnt_equivalent AS exp_tnt_equivalent,
            aexp.sound_pressure_level AS exp_sound_pressure_level,
            aexp.sound_exposure_level AS exp_sound_exposure_level,
            aexp.tnt_equivalent_actual AS exp_tnt_equivalent_actual,
            aexp.sound_pressure_level_actual AS exp_sound_pressure_level_actual,
            aexp.sound_exposure_level_actual AS exp_sound_exposure_level_actual,
            aadd.frequency AS add_frequency,
            aadd.sound_pressure_level AS add_sound_pressure_level,
            aadd.sound_exposure_level AS add_sound_exposure_level,
            aadd.frequency_actual AS add_frequency_actual,
            aadd.sound_pressure_level_actual AS add_sound_pressure_level_actual,
            aadd.sound_exposure_level_actual AS add_sound_exposure_level_actual,
            ambes.frequency AS mbes_frequency,
            ambes.sound_pressure_level AS mbes_sound_pressure_level,
            ambes.sound_exposure_level AS mbes_sound_exposure_level,
            ambes.frequency_actual AS mbes_frequency_actual,
            ambes.sound_pressure_level_actual AS mbes_sound_pressure_level_actual,
            ambes.sound_exposure_level_actual AS mbes_sound_exposure_level_actual,
            aloc.id AS actlocn_id,
            aloc.entered_ogb_code,
            public.st_astext(aloc.entered_point) AS point_text,
            public.st_astext(aloc.entered_polygon) AS polygon_text,
            aloc.creation_type,
            aloc.decomposed,
            aloc.no_activity,
            ogb.block_code,
            ogb.quadrant,
            ogb.lessthan_five,
            ogb.tw_code,
            ogb.split_block,
            ogb.assignment_block_code,
            ogb.point_req
           FROM ((((((((((((((jncc.activityapplication aapp
             LEFT JOIN jncc.noiseproducer np ON ((np.id = aapp.noiseproducer_id)))
             LEFT JOIN jncc.organisation nporg ON ((nporg.id = np.organisation_id)))
             LEFT JOIN jncc.regulator reg ON ((reg.id = aapp.regulator_id)))
             LEFT JOIN jncc.organisation regorg ON ((regorg.id = reg.organisation_id)))
             LEFT JOIN jncc.activitytype atyp ON ((atyp.id = aapp.activitytype_id)))
             LEFT JOIN jncc.activityseismic aseismic ON ((aseismic.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activitysubbottomprofilers asbp ON ((asbp.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activitypiling apiling ON ((apiling.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activityexplosives aexp ON ((aexp.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activityacousticdd aadd ON ((aadd.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activitymultibeames ambes ON ((ambes.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activitymod amod ON ((amod.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activitylocation aloc ON ((aloc.activityapplication_id = aapp.id)))
             JOIN jncc.oilandgasblock ogb ON (public.st_intersects(ogb.geom, aloc.entered_point)))
          WHERE (aloc.entered_point IS NOT NULL)
        UNION
         SELECT nporg.organisation_name AS noise_producer,
            regorg.organisation_name AS regulator,
            atyp.name AS activity_type,
            aapp.id AS aan,
            aapp.status,
            aapp.non_licensable,
            aapp.date_start,
            aapp.date_end,
            aapp.date_due,
            aapp.date_closed,
            aapp.date_updated,
            aapp.duration,
            aapp.parent_id,
            aseismic.survey_type AS seismic_survey_type,
            aseismic.data_type AS seismic_data_type,
            aseismic.other_survey_type AS seismic_other_survey_type,
            aseismic.max_airgun_volume AS seismic_max_airgun_volume,
            aseismic.sound_pressure_level AS seismic_sound_pressure_level,
            aseismic.sound_exposure_level AS seismic_sound_exposure_level,
            aseismic.max_airgun_volume_actual AS seismic_max_airgun_volume_actual,
            aseismic.sound_pressure_level_actual AS seismic_sound_pressure_level_actual,
            aseismic.sound_exposure_level_actual AS seismic_sound_exposure_level_actual,
            asbp.source AS sbp_source,
            asbp.frequency AS sbp_frequency,
            asbp.sound_pressure_level AS sbp_sound_pressure_level,
            asbp.sound_exposure_level AS sbp_sound_exposure_level,
            asbp.frequency_actual AS sbp_frequency_actual,
            asbp.sound_pressure_level_actual AS sbp_sound_pressure_level_actual,
            asbp.sound_exposure_level_actual AS sbp_sound_exposure_level_actual,
            apiling.max_hammer_energy AS piling_max_hammer_energy,
            apiling.sound_pressure_level AS piling_sound_pressure_level,
            apiling.sound_exposure_level AS piling_sound_exposure_level,
            apiling.max_hammer_energy_actual AS piling_max_hammer_energy_actual,
            apiling.sound_pressure_level_actual AS piling_sound_pressure_level_actual,
            apiling.sound_exposure_level_actual AS piling_sound_exposure_level_actual,
            aexp.tnt_equivalent AS exp_tnt_equivalent,
            aexp.sound_pressure_level AS exp_sound_pressure_level,
            aexp.sound_exposure_level AS exp_sound_exposure_level,
            aexp.tnt_equivalent_actual AS exp_tnt_equivalent_actual,
            aexp.sound_pressure_level_actual AS exp_sound_pressure_level_actual,
            aexp.sound_exposure_level_actual AS exp_sound_exposure_level_actual,
            aadd.frequency AS add_frequency,
            aadd.sound_pressure_level AS add_sound_pressure_level,
            aadd.sound_exposure_level AS add_sound_exposure_level,
            aadd.frequency_actual AS add_frequency_actual,
            aadd.sound_pressure_level_actual AS add_sound_pressure_level_actual,
            aadd.sound_exposure_level_actual AS add_sound_exposure_level_actual,
            ambes.frequency AS mbes_frequency,
            ambes.sound_pressure_level AS mbes_sound_pressure_level,
            ambes.sound_exposure_level AS mbes_sound_exposure_level,
            ambes.frequency_actual AS mbes_frequency_actual,
            ambes.sound_pressure_level_actual AS mbes_sound_pressure_level_actual,
            ambes.sound_exposure_level_actual AS mbes_sound_exposure_level_actual,
            aloc.id AS actlocn_id,
            aloc.entered_ogb_code,
            public.st_astext(aloc.entered_point) AS point_text,
            public.st_astext(aloc.entered_polygon) AS polygon_text,
            aloc.creation_type,
            aloc.decomposed,
            aloc.no_activity,
            ogb.block_code,
            ogb.quadrant,
            ogb.lessthan_five,
            ogb.tw_code,
            ogb.split_block,
            ogb.assignment_block_code,
            ogb.point_req
           FROM ((((((((((((((jncc.activityapplication aapp
             LEFT JOIN jncc.noiseproducer np ON ((np.id = aapp.noiseproducer_id)))
             LEFT JOIN jncc.organisation nporg ON ((nporg.id = np.organisation_id)))
             LEFT JOIN jncc.regulator reg ON ((reg.id = aapp.regulator_id)))
             LEFT JOIN jncc.organisation regorg ON ((regorg.id = reg.organisation_id)))
             LEFT JOIN jncc.activitytype atyp ON ((atyp.id = aapp.activitytype_id)))
             LEFT JOIN jncc.activityseismic aseismic ON ((aseismic.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activitysubbottomprofilers asbp ON ((asbp.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activitypiling apiling ON ((apiling.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activityexplosives aexp ON ((aexp.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activityacousticdd aadd ON ((aadd.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activitymultibeames ambes ON ((ambes.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activitymod amod ON ((amod.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activitylocation aloc ON ((aloc.activityapplication_id = aapp.id)))
             JOIN jncc.oilandgasblock ogb ON (public.st_intersects(ogb.geom, aloc.entered_polygon)))
          WHERE (aloc.entered_polygon IS NOT NULL)
        UNION
         SELECT nporg.organisation_name AS noise_producer,
            regorg.organisation_name AS regulator,
            atyp.name AS activity_type,
            aapp.id AS aan,
            aapp.status,
            aapp.non_licensable,
            aapp.date_start,
            aapp.date_end,
            aapp.date_due,
            aapp.date_closed,
            aapp.date_updated,
            aapp.duration,
            aapp.parent_id,
            aseismic.survey_type AS seismic_survey_type,
            aseismic.data_type AS seismic_data_type,
            aseismic.other_survey_type AS seismic_other_survey_type,
            aseismic.max_airgun_volume AS seismic_max_airgun_volume,
            aseismic.sound_pressure_level AS seismic_sound_pressure_level,
            aseismic.sound_exposure_level AS seismic_sound_exposure_level,
            aseismic.max_airgun_volume_actual AS seismic_max_airgun_volume_actual,
            aseismic.sound_pressure_level_actual AS seismic_sound_pressure_level_actual,
            aseismic.sound_exposure_level_actual AS seismic_sound_exposure_level_actual,
            asbp.source AS sbp_source,
            asbp.frequency AS sbp_frequency,
            asbp.sound_pressure_level AS sbp_sound_pressure_level,
            asbp.sound_exposure_level AS sbp_sound_exposure_level,
            asbp.frequency_actual AS sbp_frequency_actual,
            asbp.sound_pressure_level_actual AS sbp_sound_pressure_level_actual,
            asbp.sound_exposure_level_actual AS sbp_sound_exposure_level_actual,
            apiling.max_hammer_energy AS piling_max_hammer_energy,
            apiling.sound_pressure_level AS piling_sound_pressure_level,
            apiling.sound_exposure_level AS piling_sound_exposure_level,
            apiling.max_hammer_energy_actual AS piling_max_hammer_energy_actual,
            apiling.sound_pressure_level_actual AS piling_sound_pressure_level_actual,
            apiling.sound_exposure_level_actual AS piling_sound_exposure_level_actual,
            aexp.tnt_equivalent AS exp_tnt_equivalent,
            aexp.sound_pressure_level AS exp_sound_pressure_level,
            aexp.sound_exposure_level AS exp_sound_exposure_level,
            aexp.tnt_equivalent_actual AS exp_tnt_equivalent_actual,
            aexp.sound_pressure_level_actual AS exp_sound_pressure_level_actual,
            aexp.sound_exposure_level_actual AS exp_sound_exposure_level_actual,
            aadd.frequency AS add_frequency,
            aadd.sound_pressure_level AS add_sound_pressure_level,
            aadd.sound_exposure_level AS add_sound_exposure_level,
            aadd.frequency_actual AS add_frequency_actual,
            aadd.sound_pressure_level_actual AS add_sound_pressure_level_actual,
            aadd.sound_exposure_level_actual AS add_sound_exposure_level_actual,
            ambes.frequency AS mbes_frequency,
            ambes.sound_pressure_level AS mbes_sound_pressure_level,
            ambes.sound_exposure_level AS mbes_sound_exposure_level,
            ambes.frequency_actual AS mbes_frequency_actual,
            ambes.sound_pressure_level_actual AS mbes_sound_pressure_level_actual,
            ambes.sound_exposure_level_actual AS mbes_sound_exposure_level_actual,
            aloc.id AS actlocn_id,
            aloc.entered_ogb_code,
            public.st_astext(aloc.entered_point) AS point_text,
            public.st_astext(aloc.entered_polygon) AS polygon_text,
            aloc.creation_type,
            aloc.decomposed,
            aloc.no_activity,
            ogb.block_code,
            ogb.quadrant,
            ogb.lessthan_five,
            ogb.tw_code,
            ogb.split_block,
            ogb.assignment_block_code,
            ogb.point_req
           FROM ((((((((((((((jncc.activityapplication aapp
             LEFT JOIN jncc.noiseproducer np ON ((np.id = aapp.noiseproducer_id)))
             LEFT JOIN jncc.organisation nporg ON ((nporg.id = np.organisation_id)))
             LEFT JOIN jncc.regulator reg ON ((reg.id = aapp.regulator_id)))
             LEFT JOIN jncc.organisation regorg ON ((regorg.id = reg.organisation_id)))
             LEFT JOIN jncc.activitytype atyp ON ((atyp.id = aapp.activitytype_id)))
             LEFT JOIN jncc.activityseismic aseismic ON ((aseismic.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activitysubbottomprofilers asbp ON ((asbp.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activitypiling apiling ON ((apiling.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activityexplosives aexp ON ((aexp.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activityacousticdd aadd ON ((aadd.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activitymultibeames ambes ON ((ambes.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activitymod amod ON ((amod.activityapplication_id = aapp.id)))
             LEFT JOIN jncc.activitylocation aloc ON ((aloc.activityapplication_id = aapp.id)))
             JOIN jncc.oilandgasblock ogb ON (((ogb.block_code)::text = (aloc.entered_ogb_code)::text)))
          WHERE (aloc.entered_ogb_code IS NOT NULL)) block_data;


ALTER TABLE jncc.activityapplicationblocks OWNER TO jncc;

--
-- TOC entry 222 (class 1259 OID 19847)
-- Name: activityapplicationparloc; Type: VIEW; Schema: jncc; Owner: postgres
--

CREATE VIEW jncc.activityapplicationparloc AS
 SELECT activityapplication.id,
    activityapplication.date_closed,
    activityapplication.date_due,
    activityapplication.date_end,
    activityapplication.date_start,
    activityapplication.date_updated,
    activityapplication.duration,
    activityapplication.status,
    activityapplication.regulator_id,
    activityapplication.noiseproducer_id,
    activityapplication.activitytype_id,
    activityapplication.non_licensable,
    activityapplication.parent_id,
    activityapplication.use_parent_location,
    activityapplication.supp_info,
        CASE
            WHEN activityapplication.use_parent_location THEN activityapplication.parent_id
            ELSE activityapplication.id
        END AS locjoin
   FROM jncc.activityapplication;


ALTER TABLE jncc.activityapplicationparloc OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 19851)
-- Name: activityexplosives_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.activityexplosives_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.activityexplosives_id_seq OWNER TO jncc;

--
-- TOC entry 3808 (class 0 OID 0)
-- Dependencies: 223
-- Name: activityexplosives_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.activityexplosives_id_seq OWNED BY jncc.activityexplosives.id;


--
-- TOC entry 224 (class 1259 OID 19853)
-- Name: activitylocation_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.activitylocation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.activitylocation_id_seq OWNER TO jncc;

--
-- TOC entry 3809 (class 0 OID 0)
-- Dependencies: 224
-- Name: activitylocation_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.activitylocation_id_seq OWNED BY jncc.activitylocation.id;


--
-- TOC entry 225 (class 1259 OID 19855)
-- Name: activitylocationdate; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.activitylocationdate (
    id integer NOT NULL,
    activity_date date,
    activitylocation_id integer NOT NULL
);


ALTER TABLE jncc.activitylocationdate OWNER TO jncc;

--
-- TOC entry 226 (class 1259 OID 19858)
-- Name: activitylocationdate_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.activitylocationdate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.activitylocationdate_id_seq OWNER TO jncc;

--
-- TOC entry 3810 (class 0 OID 0)
-- Dependencies: 226
-- Name: activitylocationdate_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.activitylocationdate_id_seq OWNED BY jncc.activitylocationdate.id;


--
-- TOC entry 227 (class 1259 OID 19860)
-- Name: activitymod_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.activitymod_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.activitymod_id_seq OWNER TO jncc;

--
-- TOC entry 3811 (class 0 OID 0)
-- Dependencies: 227
-- Name: activitymod_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.activitymod_id_seq OWNED BY jncc.activitymod.id;


--
-- TOC entry 228 (class 1259 OID 19862)
-- Name: activitymultibeames_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.activitymultibeames_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.activitymultibeames_id_seq OWNER TO jncc;

--
-- TOC entry 3812 (class 0 OID 0)
-- Dependencies: 228
-- Name: activitymultibeames_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.activitymultibeames_id_seq OWNED BY jncc.activitymultibeames.id;


--
-- TOC entry 229 (class 1259 OID 19864)
-- Name: activitypiling_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.activitypiling_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.activitypiling_id_seq OWNER TO jncc;

--
-- TOC entry 3813 (class 0 OID 0)
-- Dependencies: 229
-- Name: activitypiling_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.activitypiling_id_seq OWNED BY jncc.activitypiling.id;


--
-- TOC entry 230 (class 1259 OID 19866)
-- Name: activityseismic_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.activityseismic_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.activityseismic_id_seq OWNER TO jncc;

--
-- TOC entry 3814 (class 0 OID 0)
-- Dependencies: 230
-- Name: activityseismic_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.activityseismic_id_seq OWNED BY jncc.activityseismic.id;


--
-- TOC entry 231 (class 1259 OID 19868)
-- Name: activitysubbottomprofilers_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.activitysubbottomprofilers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.activitysubbottomprofilers_id_seq OWNER TO jncc;

--
-- TOC entry 3815 (class 0 OID 0)
-- Dependencies: 231
-- Name: activitysubbottomprofilers_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.activitysubbottomprofilers_id_seq OWNED BY jncc.activitysubbottomprofilers.id;


--
-- TOC entry 232 (class 1259 OID 19870)
-- Name: activitytype_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.activitytype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.activitytype_id_seq OWNER TO jncc;

--
-- TOC entry 3816 (class 0 OID 0)
-- Dependencies: 232
-- Name: activitytype_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.activitytype_id_seq OWNED BY jncc.activitytype.id;


--
-- TOC entry 233 (class 1259 OID 19872)
-- Name: appuser; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.appuser (
    id integer NOT NULL,
    date_last_login timestamp with time zone,
    date_registered timestamp with time zone,
    email_address character varying(50),
    fullname character varying(50) NOT NULL,
    phone character varying(20) NOT NULL,
    password character varying(255),
    verification_status character varying(20),
    verification_token character varying(50),
    auth_token character varying(50),
    auth_token_expiry timestamp with time zone,
    date_last_sent_verification timestamp without time zone
);


ALTER TABLE jncc.appuser OWNER TO jncc;

--
-- TOC entry 234 (class 1259 OID 19878)
-- Name: appuser_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.appuser_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.appuser_id_seq OWNER TO jncc;

--
-- TOC entry 3817 (class 0 OID 0)
-- Dependencies: 234
-- Name: appuser_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.appuser_id_seq OWNED BY jncc.appuser.id;


--
-- TOC entry 235 (class 1259 OID 19880)
-- Name: audittrail; Type: TABLE; Schema: jncc; Owner: postgres
--

CREATE TABLE jncc.audittrail (
    id integer NOT NULL,
    who character varying(50) NOT NULL,
    date_when timestamp without time zone NOT NULL,
    tablename character varying(100) NOT NULL,
    event character varying(100) NOT NULL,
    fk_id integer NOT NULL,
    reason character varying(1500) NOT NULL
);


ALTER TABLE jncc.audittrail OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 19886)
-- Name: audittrail_id_seq; Type: SEQUENCE; Schema: jncc; Owner: postgres
--

CREATE SEQUENCE jncc.audittrail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.audittrail_id_seq OWNER TO postgres;

--
-- TOC entry 3819 (class 0 OID 0)
-- Dependencies: 236
-- Name: audittrail_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: postgres
--

ALTER SEQUENCE jncc.audittrail_id_seq OWNED BY jncc.audittrail.id;


--
-- TOC entry 237 (class 1259 OID 19888)
-- Name: databasechangelog; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.databasechangelog (
    id character varying(255) NOT NULL,
    author character varying(255) NOT NULL,
    filename character varying(255) NOT NULL,
    dateexecuted timestamp with time zone NOT NULL,
    orderexecuted integer NOT NULL,
    exectype character varying(10) NOT NULL,
    md5sum character varying(35),
    description character varying(255),
    comments character varying(255),
    tag character varying(255),
    liquibase character varying(20),
    contexts character varying(255),
    labels character varying(255),
    deployment_id character varying(10)
);


ALTER TABLE jncc.databasechangelog OWNER TO jncc;

--
-- TOC entry 238 (class 1259 OID 19894)
-- Name: databasechangeloglock; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.databasechangeloglock (
    id integer NOT NULL,
    locked boolean NOT NULL,
    lockgranted timestamp with time zone,
    lockedby character varying(255)
);


ALTER TABLE jncc.databasechangeloglock OWNER TO jncc;

--
-- TOC entry 239 (class 1259 OID 19897)
-- Name: noiseproducer_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.noiseproducer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.noiseproducer_id_seq OWNER TO jncc;

--
-- TOC entry 3821 (class 0 OID 0)
-- Dependencies: 239
-- Name: noiseproducer_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.noiseproducer_id_seq OWNED BY jncc.noiseproducer.id;


--
-- TOC entry 240 (class 1259 OID 19899)
-- Name: oilandgasblock_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.oilandgasblock_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.oilandgasblock_id_seq OWNER TO jncc;

--
-- TOC entry 3822 (class 0 OID 0)
-- Dependencies: 240
-- Name: oilandgasblock_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.oilandgasblock_id_seq OWNED BY jncc.oilandgasblock.id;


--
-- TOC entry 241 (class 1259 OID 19901)
-- Name: organisation_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.organisation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.organisation_id_seq OWNER TO jncc;

--
-- TOC entry 3823 (class 0 OID 0)
-- Dependencies: 241
-- Name: organisation_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.organisation_id_seq OWNED BY jncc.organisation.id;


--
-- TOC entry 242 (class 1259 OID 19903)
-- Name: orguser; Type: TABLE; Schema: jncc; Owner: jncc
--

CREATE TABLE jncc.orguser (
    id integer NOT NULL,
    appuser_id integer NOT NULL,
    organisation_id integer NOT NULL,
    administrator boolean DEFAULT false,
    status character varying(20) DEFAULT 'unverified'::character varying
);


ALTER TABLE jncc.orguser OWNER TO jncc;

--
-- TOC entry 243 (class 1259 OID 19908)
-- Name: orguser_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.orguser_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.orguser_id_seq OWNER TO jncc;

--
-- TOC entry 3824 (class 0 OID 0)
-- Dependencies: 243
-- Name: orguser_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.orguser_id_seq OWNED BY jncc.orguser.id;


--
-- TOC entry 244 (class 1259 OID 19910)
-- Name: regulator_id_seq; Type: SEQUENCE; Schema: jncc; Owner: jncc
--

CREATE SEQUENCE jncc.regulator_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jncc.regulator_id_seq OWNER TO jncc;

--
-- TOC entry 3825 (class 0 OID 0)
-- Dependencies: 244
-- Name: regulator_id_seq; Type: SEQUENCE OWNED BY; Schema: jncc; Owner: jncc
--

ALTER SEQUENCE jncc.regulator_id_seq OWNED BY jncc.regulator.id;


--
-- TOC entry 3592 (class 2604 OID 19928)
-- Name: activityacousticdd id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activityacousticdd ALTER COLUMN id SET DEFAULT nextval('jncc.activityacousticdd_id_seq'::regclass);


--
-- TOC entry 3595 (class 2604 OID 19929)
-- Name: activityapplication id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activityapplication ALTER COLUMN id SET DEFAULT nextval('jncc.activityapplication_id_seq'::regclass);


--
-- TOC entry 3596 (class 2604 OID 19930)
-- Name: activityexplosives id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activityexplosives ALTER COLUMN id SET DEFAULT nextval('jncc.activityexplosives_id_seq'::regclass);


--
-- TOC entry 3598 (class 2604 OID 19931)
-- Name: activitylocation id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitylocation ALTER COLUMN id SET DEFAULT nextval('jncc.activitylocation_id_seq'::regclass);


--
-- TOC entry 3611 (class 2604 OID 19932)
-- Name: activitylocationdate id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitylocationdate ALTER COLUMN id SET DEFAULT nextval('jncc.activitylocationdate_id_seq'::regclass);


--
-- TOC entry 3599 (class 2604 OID 19933)
-- Name: activitymod id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitymod ALTER COLUMN id SET DEFAULT nextval('jncc.activitymod_id_seq'::regclass);


--
-- TOC entry 3600 (class 2604 OID 19934)
-- Name: activitymultibeames id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitymultibeames ALTER COLUMN id SET DEFAULT nextval('jncc.activitymultibeames_id_seq'::regclass);


--
-- TOC entry 3601 (class 2604 OID 19935)
-- Name: activitypiling id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitypiling ALTER COLUMN id SET DEFAULT nextval('jncc.activitypiling_id_seq'::regclass);


--
-- TOC entry 3602 (class 2604 OID 19936)
-- Name: activityseismic id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activityseismic ALTER COLUMN id SET DEFAULT nextval('jncc.activityseismic_id_seq'::regclass);


--
-- TOC entry 3603 (class 2604 OID 19937)
-- Name: activitysubbottomprofilers id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitysubbottomprofilers ALTER COLUMN id SET DEFAULT nextval('jncc.activitysubbottomprofilers_id_seq'::regclass);


--
-- TOC entry 3604 (class 2604 OID 19938)
-- Name: activitytype id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitytype ALTER COLUMN id SET DEFAULT nextval('jncc.activitytype_id_seq'::regclass);


--
-- TOC entry 3612 (class 2604 OID 19939)
-- Name: appuser id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.appuser ALTER COLUMN id SET DEFAULT nextval('jncc.appuser_id_seq'::regclass);


--
-- TOC entry 3613 (class 2604 OID 19940)
-- Name: audittrail id; Type: DEFAULT; Schema: jncc; Owner: postgres
--

ALTER TABLE ONLY jncc.audittrail ALTER COLUMN id SET DEFAULT nextval('jncc.audittrail_id_seq'::regclass);


--
-- TOC entry 3605 (class 2604 OID 19941)
-- Name: noiseproducer id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.noiseproducer ALTER COLUMN id SET DEFAULT nextval('jncc.noiseproducer_id_seq'::regclass);


--
-- TOC entry 3606 (class 2604 OID 19942)
-- Name: oilandgasblock id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.oilandgasblock ALTER COLUMN id SET DEFAULT nextval('jncc.oilandgasblock_id_seq'::regclass);


--
-- TOC entry 3608 (class 2604 OID 19943)
-- Name: organisation id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.organisation ALTER COLUMN id SET DEFAULT nextval('jncc.organisation_id_seq'::regclass);


--
-- TOC entry 3616 (class 2604 OID 19944)
-- Name: orguser id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.orguser ALTER COLUMN id SET DEFAULT nextval('jncc.orguser_id_seq'::regclass);


--
-- TOC entry 3610 (class 2604 OID 19945)
-- Name: regulator id; Type: DEFAULT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.regulator ALTER COLUMN id SET DEFAULT nextval('jncc.regulator_id_seq'::regclass);


--
-- TOC entry 3618 (class 2606 OID 19949)
-- Name: Xuser XuserPK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc."Xuser"
    ADD CONSTRAINT "XuserPK" PRIMARY KEY (email);


--
-- TOC entry 3620 (class 2606 OID 19951)
-- Name: activityacousticdd activityacousPK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activityacousticdd
    ADD CONSTRAINT "activityacousPK" PRIMARY KEY (id);


--
-- TOC entry 3622 (class 2606 OID 19953)
-- Name: activityapplication activityappliPK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activityapplication
    ADD CONSTRAINT "activityappliPK" PRIMARY KEY (id);


--
-- TOC entry 3624 (class 2606 OID 19955)
-- Name: activityexplosives activityexploPK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activityexplosives
    ADD CONSTRAINT "activityexploPK" PRIMARY KEY (id);


--
-- TOC entry 3636 (class 2606 OID 19957)
-- Name: activitysubbottomprofilers activitygeophPK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitysubbottomprofilers
    ADD CONSTRAINT "activitygeophPK" PRIMARY KEY (id);


--
-- TOC entry 3626 (class 2606 OID 19959)
-- Name: activitylocation activitylocatPK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitylocation
    ADD CONSTRAINT "activitylocatPK" PRIMARY KEY (id);


--
-- TOC entry 3628 (class 2606 OID 19961)
-- Name: activitymod activitymodPK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitymod
    ADD CONSTRAINT "activitymodPK" PRIMARY KEY (id);


--
-- TOC entry 3630 (class 2606 OID 19963)
-- Name: activitymultibeames activitymultiPK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitymultibeames
    ADD CONSTRAINT "activitymultiPK" PRIMARY KEY (id);


--
-- TOC entry 3632 (class 2606 OID 19965)
-- Name: activitypiling activitypilinPK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitypiling
    ADD CONSTRAINT "activitypilinPK" PRIMARY KEY (id);


--
-- TOC entry 3634 (class 2606 OID 19967)
-- Name: activityseismic activityseismPK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activityseismic
    ADD CONSTRAINT "activityseismPK" PRIMARY KEY (id);


--
-- TOC entry 3638 (class 2606 OID 19969)
-- Name: activitytype activitytypePK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitytype
    ADD CONSTRAINT "activitytypePK" PRIMARY KEY (id);


--
-- TOC entry 3646 (class 2606 OID 19971)
-- Name: activitylocationdate actlocdatePK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitylocationdate
    ADD CONSTRAINT "actlocdatePK" PRIMARY KEY (id);


--
-- TOC entry 3648 (class 2606 OID 19973)
-- Name: appuser appuserPK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.appuser
    ADD CONSTRAINT "appuserPK" PRIMARY KEY (id);


--
-- TOC entry 3652 (class 2606 OID 19975)
-- Name: audittrail audittrailPK; Type: CONSTRAINT; Schema: jncc; Owner: postgres
--

ALTER TABLE ONLY jncc.audittrail
    ADD CONSTRAINT "audittrailPK" PRIMARY KEY (id);


--
-- TOC entry 3640 (class 2606 OID 19977)
-- Name: noiseproducer noiseproducerPK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.noiseproducer
    ADD CONSTRAINT "noiseproducerPK" PRIMARY KEY (id);


--
-- TOC entry 3642 (class 2606 OID 19979)
-- Name: organisation organisationPK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.organisation
    ADD CONSTRAINT "organisationPK" PRIMARY KEY (id);


--
-- TOC entry 3656 (class 2606 OID 19981)
-- Name: orguser orguserPK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.orguser
    ADD CONSTRAINT "orguserPK" PRIMARY KEY (id);


--
-- TOC entry 3654 (class 2606 OID 19983)
-- Name: databasechangeloglock pk_databasechangeloglock; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.databasechangeloglock
    ADD CONSTRAINT pk_databasechangeloglock PRIMARY KEY (id);


--
-- TOC entry 3644 (class 2606 OID 19985)
-- Name: regulator regulatorPK; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.regulator
    ADD CONSTRAINT "regulatorPK" PRIMARY KEY (id);


--
-- TOC entry 3650 (class 2606 OID 19987)
-- Name: appuser uc_appuseremail_address_col; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.appuser
    ADD CONSTRAINT uc_appuseremail_address_col UNIQUE (email_address);


--
-- TOC entry 3658 (class 2606 OID 19989)
-- Name: orguser uc_orguser_appuser_id_organisation_id; Type: CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.orguser
    ADD CONSTRAINT uc_orguser_appuser_id_organisation_id UNIQUE (appuser_id, organisation_id);


--
-- TOC entry 3659 (class 2606 OID 19993)
-- Name: activityacousticdd FK_3mvcr34ryb1siy3lpjtum2lb6; Type: FK CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activityacousticdd
    ADD CONSTRAINT "FK_3mvcr34ryb1siy3lpjtum2lb6" FOREIGN KEY (activityapplication_id) REFERENCES jncc.activityapplication(id);


--
-- TOC entry 3663 (class 2606 OID 19998)
-- Name: activityexplosives FK_5b4skn8vslqgwu7qf4dvwe64c; Type: FK CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activityexplosives
    ADD CONSTRAINT "FK_5b4skn8vslqgwu7qf4dvwe64c" FOREIGN KEY (activityapplication_id) REFERENCES jncc.activityapplication(id);


--
-- TOC entry 3672 (class 2606 OID 20003)
-- Name: activitylocationdate FK_7jq8usxi2ql0fw6fk15fhi3wi; Type: FK CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitylocationdate
    ADD CONSTRAINT "FK_7jq8usxi2ql0fw6fk15fhi3wi" FOREIGN KEY (activitylocation_id) REFERENCES jncc.activitylocation(id);


--
-- TOC entry 3665 (class 2606 OID 20008)
-- Name: activitymod FK_activitymod_activityapp; Type: FK CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitymod
    ADD CONSTRAINT "FK_activitymod_activityapp" FOREIGN KEY (activityapplication_id) REFERENCES jncc.activityapplication(id);


--
-- TOC entry 3669 (class 2606 OID 20013)
-- Name: activitysubbottomprofilers FK_clhcxfrbcnnsfef53niqjaxqp; Type: FK CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitysubbottomprofilers
    ADD CONSTRAINT "FK_clhcxfrbcnnsfef53niqjaxqp" FOREIGN KEY (activityapplication_id) REFERENCES jncc.activityapplication(id);


--
-- TOC entry 3660 (class 2606 OID 20018)
-- Name: activityapplication FK_ek1stjpclcljj1189kellqfi0; Type: FK CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activityapplication
    ADD CONSTRAINT "FK_ek1stjpclcljj1189kellqfi0" FOREIGN KEY (regulator_id) REFERENCES jncc.regulator(id);


--
-- TOC entry 3673 (class 2606 OID 20023)
-- Name: orguser FK_fhs5vvkrq8ddaj8ydjnqj7fad; Type: FK CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.orguser
    ADD CONSTRAINT "FK_fhs5vvkrq8ddaj8ydjnqj7fad" FOREIGN KEY (appuser_id) REFERENCES jncc.appuser(id);


--
-- TOC entry 3661 (class 2606 OID 20028)
-- Name: activityapplication FK_g6l7033ygee8hx9by3oepa92s; Type: FK CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activityapplication
    ADD CONSTRAINT "FK_g6l7033ygee8hx9by3oepa92s" FOREIGN KEY (noiseproducer_id) REFERENCES jncc.noiseproducer(id);


--
-- TOC entry 3671 (class 2606 OID 20033)
-- Name: regulator FK_hniwxi8cgdasdvfudyxk75a3e; Type: FK CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.regulator
    ADD CONSTRAINT "FK_hniwxi8cgdasdvfudyxk75a3e" FOREIGN KEY (organisation_id) REFERENCES jncc.organisation(id);


--
-- TOC entry 3667 (class 2606 OID 20038)
-- Name: activitypiling FK_jdm4kevgkjlsthi4ytpu7cqpg; Type: FK CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitypiling
    ADD CONSTRAINT "FK_jdm4kevgkjlsthi4ytpu7cqpg" FOREIGN KEY (activityapplication_id) REFERENCES jncc.activityapplication(id);


--
-- TOC entry 3674 (class 2606 OID 20043)
-- Name: orguser FK_k4au2oyrrj8qjs303uhd7p3gg; Type: FK CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.orguser
    ADD CONSTRAINT "FK_k4au2oyrrj8qjs303uhd7p3gg" FOREIGN KEY (organisation_id) REFERENCES jncc.organisation(id);


--
-- TOC entry 3670 (class 2606 OID 20048)
-- Name: noiseproducer FK_kq0eauek1nijbnc7n0p12f7nv; Type: FK CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.noiseproducer
    ADD CONSTRAINT "FK_kq0eauek1nijbnc7n0p12f7nv" FOREIGN KEY (organisation_id) REFERENCES jncc.organisation(id);


--
-- TOC entry 3666 (class 2606 OID 20053)
-- Name: activitymultibeames FK_kwaqfvmvae4funmnow185jod1; Type: FK CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitymultibeames
    ADD CONSTRAINT "FK_kwaqfvmvae4funmnow185jod1" FOREIGN KEY (activityapplication_id) REFERENCES jncc.activityapplication(id);


--
-- TOC entry 3662 (class 2606 OID 20058)
-- Name: activityapplication FK_rxkbpwfl8cg4j5sbo1p26c3t8; Type: FK CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activityapplication
    ADD CONSTRAINT "FK_rxkbpwfl8cg4j5sbo1p26c3t8" FOREIGN KEY (parent_id) REFERENCES jncc.activityapplication(id);


--
-- TOC entry 3664 (class 2606 OID 20063)
-- Name: activitylocation FK_sqt5gh3w3lkst5sshffmfgpk4; Type: FK CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activitylocation
    ADD CONSTRAINT "FK_sqt5gh3w3lkst5sshffmfgpk4" FOREIGN KEY (activityapplication_id) REFERENCES jncc.activityapplication(id);


--
-- TOC entry 3668 (class 2606 OID 20068)
-- Name: activityseismic FK_wqltvlpdiqm6cysoxath38cp; Type: FK CONSTRAINT; Schema: jncc; Owner: jncc
--

ALTER TABLE ONLY jncc.activityseismic
    ADD CONSTRAINT "FK_wqltvlpdiqm6cysoxath38cp" FOREIGN KEY (activityapplication_id) REFERENCES jncc.activityapplication(id);


--
-- TOC entry 3818 (class 0 OID 0)
-- Dependencies: 235
-- Name: TABLE audittrail; Type: ACL; Schema: jncc; Owner: postgres
--

GRANT ALL ON TABLE jncc.audittrail TO jncc;


--
-- TOC entry 3820 (class 0 OID 0)
-- Dependencies: 236
-- Name: SEQUENCE audittrail_id_seq; Type: ACL; Schema: jncc; Owner: postgres
--

GRANT USAGE ON SEQUENCE jncc.audittrail_id_seq TO jncc;


-- Completed on 2020-02-10 13:32:04

--
-- PostgreSQL database dump complete
--

