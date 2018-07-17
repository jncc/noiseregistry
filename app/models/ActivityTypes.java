package models;
import java.util.LinkedHashMap;

import play.i18n.Messages;

public enum ActivityTypes {
	Seismic_Survey(1L),
	Sub_Bottom_Profilers(2L),
	Piling(3L),
	Explosives(4L),
	Acoustic_Deterrent_Device(5L),
	Multibeam_Echosounders(6L),
	MoD(7L);
	
	private Long value;
	
	/**
	 * Constructor
	 * @param i the value of the type
	 */
	private ActivityTypes(Long i)
	{
		this.value=i;
	}
	/**
	 * Gets this type as a Long value
	 * @return
	 */
	public Long toLong() {
		return this.value;
	}
	/**
	 * Gets a long version of the type as a string.  The list configured in this class
	 * should match the list in the database 
	 * @param l the activity type Long value
	 * @return
	 */
	public static String get(Long l)
	{
		if (l==1L)
			return Messages.get("activity_type.seismic");
		if (l==2L)
			return Messages.get("activity_type.subbottomprofilers");
		if (l==3L)
			return Messages.get("activity_type.piling");
		if (l==4L)
			return Messages.get("activity_type.explosives");
		if (l==5L)
			return Messages.get("activity_type.add");
		if (l==6L)
			return Messages.get("activity_type.mbes");
		if (l==7L)
			return Messages.get("activity_type.mod");
		return Messages.get("activity_type.unknown");
	}
	/**
	 * Get this activity as a string
	 */
	@Override
	public String toString()
	{
		return ActivityTypes.get(this.value);
	}
	
	/**
	 * Get the activity types as a list for use in options
	 * @return
	 */
	public static LinkedHashMap<Long, String> getOptions() {
		LinkedHashMap<Long, String> options = new LinkedHashMap<Long, String>();
        for (int i=1; i<=7; i++) {
        	options.put(Long.valueOf(i), get(Long.valueOf(i)));
        }
    	return options;
	}
}