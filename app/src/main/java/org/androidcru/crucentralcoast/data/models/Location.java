package org.androidcru.crucentralcoast.data.models;

import com.google.gson.annotations.SerializedName;

import org.androidcru.crucentralcoast.AppConstants;
import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel
public final class Location
{
    public static final String sPostcode = "postcode";
    public static final String sState = "state";
    public static final String sSuburb = "suburb";
    public static final String sStreet1 = "street1";
    public static final String sCountry = "country";
    public static final String sGeo = "geo";

    @SerializedName(sPostcode) public String postcode;
    @SerializedName(sState) public String state;
    @SerializedName(sSuburb) public String suburb;
    @SerializedName(sStreet1) public String street1;
    @SerializedName(sCountry) public String country;
    @SerializedName(sGeo) public double[] geo;

    @ParcelConstructor
    public Location(String postcode, String state,
                    String suburb, String street1,
                    String country, double[] geo)
    {
        this.postcode = postcode;
        this.state = state;
        this.suburb = suburb;
        this.street1 = street1;
        this.country = country;
        this.geo = geo;
    }

    public Location(String loc, double[] geo) {
        this.geo = geo;

        String[] splitAddress = loc.split(AppConstants.SPACE_COMMA_ESCAPE);
        String[] splitStateZip = splitAddress[2].split(" ");

        this.postcode = splitStateZip.length == 2 ? splitStateZip[1] : null;
        this.state = splitStateZip.length == 2 ? splitStateZip[0] : splitAddress[2];
        this.street1 = splitAddress[0];
        this.suburb = splitAddress[1];
        this.country = splitAddress[3];
    }

    public String getAsQuery()
    {
        String locString = String.format("%s %s, %s, %s, %s", street1, suburb, state, postcode, country);
        return locString.replace(" ", "+");
    }


    public String toString()
    {
        return String.format("%s%s%s%s%s",
                street1 == null ? "" : street1,
                suburb == null ? "" : " " + suburb,
                state == null ? "" : ", " + state,
                postcode == null ? "" : ", " + postcode,
                country == null ? "" : ", " + country);
    }

    //Auto-generated equals and hashcode
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (!postcode.equals(location.postcode)) return false;
        if (!state.equals(location.state)) return false;
        if (!suburb.equals(location.suburb)) return false;
        if (!street1.equals(location.street1)) return false;
        return country.equals(location.country);

    }

    @Override
    public int hashCode()
    {
        int result = postcode.hashCode();
        result = 31 * result + state.hashCode();
        result = 31 * result + suburb.hashCode();
        result = 31 * result + street1.hashCode();
        result = 31 * result + country.hashCode();
        return result;
    }
}
