package com.rahul.kovil.common.enums;

public enum Nakshathra {

	ASWATHI("അശ്വതി", "Ashwini", "Aries"), BHARANI("ഭരണി", "Bharani", "Aries"),
	KAARTHIKA("കാർത്തിക", "Krittika", "Aries / Taurus"), ROHINI("രോഹിണി", "Rohini", "Taurus"),
	MAKAYIRAM("മകയിരം", "Mrigashira", "Taurus / Gemini"), THIRUVATHIRA("തിരുവാതിര", "Ardra", "Gemini"),
	PUNARTHAM("പുണർതം", "Punarvasu", "Gemini / Cancer"), POOYYAM("പൂയ്യം", "Pushya", "Cancer"),
	AAYILYAM("ആയില്യം", "Ashlesha", "Cancer"), MAKAM("മകം", "Magha", "Leo"), POORAM("പൂരം", "Purva Phalguni", "Leo"),
	UTHRAM("ഉത്രം", "Uttara Phalguni", "Leo / Virgo"), ATHAM("അത്തം", "Hasta", "Virgo"),
	CHITHIRA("ചിത്തിര", "Chitra", "Virgo / Libra"), CHOTHI("ചോതി", "Swati", "Libra"),
	VISHAKHAM("വിശാഖം", "Vishakha", "Libra / Scorpio"), ANIZHAM("അനിഴം", "Anuradha", "Scorpio"),
	THRIKKETTA("തൃക്കേട്ട", "Jyeshtha", "Scorpio"), MOOLAM("മൂലം", "Mula", "Sagittarius"),
	POORADAM("പൂരാടം", "Purva Ashadha", "Sagittarius"),
	UTHRADAM("ഉത്രാടം", "Uttara Ashadha", "Sagittarius / Capricorn"), THIRUVONAM("തിരുവോണം", "Shravana", "Capricorn"),
	AVITTAM("അവിട്ടം", "Dhanishta", "Capricorn / Aquarius"), CHATHAYAM("ചതയം", "Shatabhisha", "Aquarius"),
	POORURUTTATHI("പൂരുരുട്ടാതി", "Purva Bhadrapada", "Aquarius / Pisces"),
	UTHRATTATHI("ഉത്രട്ടാതി", "Uttara Bhadrapada", "Pisces"), REVATHI("രേവതി", "Revati", "Pisces");

	private final String malayalamName;
	private final String englishName;
	private final String zodiacSign;

	Nakshathra(String malayalamName, String englishName, String zodiacSign) {
		this.malayalamName = malayalamName;
		this.englishName = englishName;
		this.zodiacSign = zodiacSign;
	}

	public String getMalayalamName() {
		return malayalamName;
	}

	public String getEnglishName() {
		return englishName;
	}

	public String getZodiacSign() {
		return zodiacSign;
	}
}
