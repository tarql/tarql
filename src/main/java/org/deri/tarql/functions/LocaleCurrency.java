package org.deri.tarql.functions;

import org.apache.jena.atlas.lib.Pair;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Mappings between currency codes and dbpedia currency URIs with names. <p/> The datatypes used here were extracted
 * from the <a href="https://github.com/dbpedia/extraction-framework/blob/master/core/src/main/scala/org/dbpedia/extraction/ontology/OntologyDatatypes.scala">DBpedia
 * ontology code</a> These codes are comparable to the standard <a href="http://www.currency-iso.org/dam/downloads/table_a1.xml">ISO
 * 4217 codes</a>. Check also <a href="https://en.wikipedia.org/wiki/ISO_4217">ISO 4217 Wikipedia page</a>
 */
public class LocaleCurrency {

    public static final Map<String, Pair<String, String>> locales;

    static {
        locales = new HashMap<String, Pair<String, String>>();

        locales.put("USD", new Pair("usDollar", "US dollar"));
        locales.put("EUR", new Pair("euro", "Euro"));
        locales.put("GBP", new Pair("poundSterling", "British Pound"));
        locales.put("JPY", new Pair("japaneseYen", "Japanese yen"));
        locales.put("RUR", new Pair("russianRouble", "Russian rouble")); // also "RUB"
        locales.put("AED", new Pair("unitedArabEmiratesDirham", "United Arab Emirates dirham"));
        locales.put("AFN", new Pair("afghanAfghani", "Afghani"));
        locales.put("ALL", new Pair("albanianLek", "Lek"));
        locales.put("AMD", new Pair("armenianDram", "Armenian dram"));
        locales.put("ANG", new Pair("netherlandsAntilleanGuilder", "Netherlands Antillean guilder"));
        locales.put("AOA", new Pair("angolanKwanza", "Kwanza"));
        locales.put("ARS", new Pair("argentinePeso", "Argentine peso"));
        locales.put("AUD", new Pair("australianDollar", "Australian dollar"));
        locales.put("AWG", new Pair("arubanGuilder", "Aruban guilder"));
        locales.put("BAM", new Pair("bosniaAndHerzegovinaConvertibleMarks", "Convertible marks"));
        locales.put("BBD", new Pair("barbadosDollar", "Barbados dollar"));
        locales.put("BDT", new Pair("bangladeshiTaka", "Bangladeshi taka"));
        locales.put("BGN", new Pair("bulgarianLev", "Bulgarian lev"));
        locales.put("BHD", new Pair("bahrainiDinar", "Bahraini dinar"));
        locales.put("BIF", new Pair("burundianFranc", "Burundian franc"));
        locales.put("BMD", new Pair("bermudianDollar", "Bermudian dollar"));
        locales.put("BND", new Pair("bruneiDollar", "Brunei dollar"));
        locales.put("BOB", new Pair("bolivianBoliviano", "Boliviano"));
        locales.put("BRL", new Pair("brazilianReal", "Brazilian real"));
        locales.put("BSD", new Pair("bahamianDollar", "Bahamian dollar"));
        locales.put("BTN", new Pair("bhutaneseNgultrum", "Ngultrum"));
        locales.put("BWP", new Pair("botswanaPula", "Pula"));
        locales.put("BYR", new Pair("belarussianRuble", "Belarussian ruble"));
        locales.put("BZD", new Pair("belizeDollar", "Belize dollar"));
        locales.put("CAD", new Pair("canadianDollar", "Canadian dollar"));
        locales.put("CDF", new Pair("congoleseFranc", "Franc Congolais"));
        locales.put("CHF", new Pair("swissFranc", "Swiss franc"));
        locales.put("CLP", new Pair("chileanPeso", "Chilean peso"));
        locales.put("CNY", new Pair("renminbi", "Renminbi"));
        locales.put("COP", new Pair("colombianPeso", "Colombian peso"));
        // ??? locales.put("unidadDeValorReal", "COU","Unidad de Valor Real")));
        locales.put("CRC", new Pair("costaRicanColon", "Costa Rican colon"));
        locales.put("CUP", new Pair("cubanPeso", "Cuban peso"));
        locales.put("CVE", new Pair("capeVerdeEscudo", "Cape Verde escudo"));
        locales.put("CZK", new Pair("czechKoruna", "Czech koruna"));
        locales.put("DJF", new Pair("djiboutianFranc", "Djibouti franc"));
        locales.put("DKK", new Pair("danishKrone", "Danish krone"));
        locales.put("DOP", new Pair("dominicanPeso", "Dominican peso"));
        locales.put("DZD", new Pair("algerianDinar", "Algerian dinar"));
        locales.put("EEK", new Pair("estonianKroon", "Kroon"));
        locales.put("EGP", new Pair("egyptianPound", "Egyptian pound"));
        locales.put("ERN", new Pair("eritreanNakfa", "Nakfa"));
        locales.put("ETB", new Pair("ethiopianBirr", "Ethiopian birr"));
        locales.put("FJD", new Pair("fijiDollar", "Fiji dollar"));
        locales.put("FKP", new Pair("falklandIslandsPound", "Falkland Islands pound"));
        locales.put("GEL", new Pair("georgianLari", "Lari"));
        locales.put("GHS", new Pair("ghanaianCedi", "Cedi"));
        locales.put("GIP", new Pair("gibraltarPound", "Gibraltar pound"));
        locales.put("GMD", new Pair("gambianDalasi", "Dalasi"));
        locales.put("GNF", new Pair("guineaFranc", "Guinea franc"));
        locales.put("GTQ", new Pair("guatemalanQuetzal", "Quetzal"));
        locales.put("GYD", new Pair("guyanaDollar", "Guyana dollar"));
        locales.put("HKD", new Pair("hongKongDollar", "Hong Kong dollar"));
        locales.put("HNL", new Pair("honduranLempira", "Lempira"));
        locales.put("HRK", new Pair("croatianKuna", "Croatian kuna"));
        locales.put("HTG", new Pair("haitiGourde", "Haiti gourde"));
        locales.put("HUF", new Pair("hungarianForint", "Forint"));
        locales.put("IDR", new Pair("indonesianRupiah", "Rupiah"));
        locales.put("ILS", new Pair("israeliNewSheqel", "Israeli new sheqel"));
        locales.put("INR", new Pair("indianRupee", "Indian rupee"));
        locales.put("IQD", new Pair("iraqiDinar", "Iraqi dinar"));
        locales.put("IRR", new Pair("iranianRial", "Iranian rial"));
        locales.put("ISK", new Pair("icelandKrona", "Iceland krona"));
        locales.put("JMD", new Pair("jamaicanDollar", "Jamaican dollar"));
        locales.put("JOD", new Pair("jordanianDinar", "Jordanian dinar"));
        locales.put("KES", new Pair("kenyanShilling", "Kenyan shilling"));
        locales.put("KGS", new Pair("kyrgyzstaniSom", "Som"));
        locales.put("UZS", new Pair("uzbekistanSom", "Uzbekistan som"));
        locales.put("KHR", new Pair("cambodianRiel", "Riel"));
        locales.put("KMF", new Pair("comorianFranc", "Comoro franc"));
        locales.put("KPW", new Pair("northKoreanWon", "North Korean won"));
        locales.put("KRW", new Pair("southKoreanWon", "South Korean won"));
        locales.put("KWD", new Pair("kuwaitiDinar", "Kuwaiti dinar"));
        locales.put("KYD", new Pair("caymanIslandsDollar", "Cayman Islands dollar"));
        locales.put("KZT", new Pair("kazakhstaniTenge", "Tenge"));
        locales.put("LAK", new Pair("laoKip", "Kip"));
        locales.put("LBP", new Pair("lebanesePound", "Lebanese pound"));
        locales.put("LKR", new Pair("sriLankanRupee", "Sri Lanka rupee"));
        locales.put("LRD", new Pair("liberianDollar", "Liberian dollar"));
        locales.put("LSL", new Pair("lesothoLoti", "Loti"));
        locales.put("LTL", new Pair("lithuanianLitas", "Lithuanian litas"));
        locales.put("LVL", new Pair("latvianLats", "Latvian lats"));
        locales.put("LYD", new Pair("libyanDinar", "Libyan dinar"));
        locales.put("MAD", new Pair("moroccanDirham", "Moroccan dirham"));
        locales.put("MDL", new Pair("moldovanLeu", "Moldovan leu"));
        locales.put("MGA", new Pair("malagasyAriary", "Malagasy ariary"));
        locales.put("MKD", new Pair("macedonianDenar", "Denar"));
        locales.put("MMK", new Pair("myanmaKyat", "Kyat"));
        locales.put("MNT", new Pair("mongolianTögrög", "Tugrik"));
        locales.put("MOP", new Pair("macanesePataca", "Pataca"));
        locales.put("MRO", new Pair("mauritanianOuguiya", "Ouguiya"));
        locales.put("MUR", new Pair("mauritianRupee", "Mauritius rupee"));
        locales.put("MVR", new Pair("maldivianRufiyaa", "Rufiyaa"));
        locales.put("MWK", new Pair("malawianKwacha", "malawian kwacha")); // TODO: "kwacha" is also used, but clashes with zambianKwacha.
        locales.put("ZMK", new Pair("zambianKwacha", "zambian kwacha")); // TODO: "kwacha" is also used, but clashes with malawianKwacha.
        locales.put("MXN", new Pair("mexicanPeso", "Mexican peso"));
        locales.put("MYR", new Pair("malaysianRinggit", "Malaysian ringgit"));
        locales.put("MZN", new Pair("mozambicanMetical", "Metical"));
        locales.put("NAD", new Pair("namibianDollar", "Namibian dollar"));
        locales.put("NGN", new Pair("nigerianNaira", "Naira"));
        locales.put("NIO", new Pair("nicaraguanCórdoba", "Cordoba oro"));
        locales.put("NOK", new Pair("norwegianKrone", "Norwegian krone"));
        locales.put("NPR", new Pair("nepaleseRupee", "Nepalese rupee"));
        locales.put("NZD", new Pair("newZealandDollar", "New Zealand dollar"));
        locales.put("OMR", new Pair("omaniRial", "Rial Omani"));
        locales.put("PAB", new Pair("panamanianBalboa", "Balboa"));
        locales.put("PEN", new Pair("peruvianNuevoSol", "Nuevo sol"));
        locales.put("PGK", new Pair("papuaNewGuineanKina", "Kina"));
        locales.put("PHP", new Pair("philippinePeso", "Philippine peso"));
        locales.put("PKR", new Pair("pakistaniRupee", "Pakistan rupee"));
        locales.put("PLN", new Pair("polishZłoty", "Złoty"));
        locales.put("PYG", new Pair("paraguayanGuarani", "Guarani"));
        locales.put("QAR", new Pair("qatariRial", "Qatari rial"));
        locales.put("RON", new Pair("romanianNewLeu", "Romanian new leu"));
        locales.put("RSD", new Pair("serbianDinar", "Serbian dinar"));
        locales.put("RWF", new Pair("rwandaFranc", "Rwanda franc"));
        locales.put("SAR", new Pair("saudiRiyal", "Saudi riyal"));
        locales.put("SBD", new Pair("solomonIslandsDollar", "Solomon Islands dollar"));
        locales.put("SCR", new Pair("seychellesRupee", "Seychelles rupee"));
        locales.put("SDG", new Pair("sudanesePound", "Sudanese pound"));
        locales.put("SEK", new Pair("swedishKrona", "Swedish krona"));
        locales.put("SGD", new Pair("singaporeDollar", "Singapore dollar"));
        locales.put("SHP", new Pair("saintHelenaPound", "Saint Helena pound"));
        locales.put("SKK", new Pair("slovakKoruna", "Slovak koruna"));
        locales.put("SLL", new Pair("sierraLeoneanLeone", "Leone"));
        locales.put("SOS", new Pair("somaliShilling", "Somali shilling"));
        locales.put("SRD", new Pair("surinamDollar", "Surinam dollar"));
        locales.put("STD", new Pair("sãoToméAndPríncipeDobra", "Dobra"));
        locales.put("SYP", new Pair("syrianPound", "Syrian pound"));
        locales.put("SZL", new Pair("swaziLilangeni", "Lilangeni"));
        locales.put("THB", new Pair("thaiBaht", "Baht"));
        locales.put("TJS", new Pair("tajikistaniSomoni", "Somoni"));
        locales.put("TMT", new Pair("turkmenistaniManat", "turkmenistani manat")); // old code: TMM
        locales.put("AZN", new Pair("azerbaijaniManat", "azerbaijanian manat"));
        locales.put("TND", new Pair("tunisianDinar", "Tunisian dinar"));
        locales.put("TOP", new Pair("tonganPaanga", "Paanga")); // correct: "Tongan Paʻanga"
        locales.put("TRY", new Pair("turkishLira", "turkish lira"));
        locales.put("TTD", new Pair("trinidadAndTobagoDollar", "Trinidad and Tobago dollar"));
        locales.put("TWD", new Pair("newTaiwanDollar", "New Taiwan dollar"));
        locales.put("TZS", new Pair("tanzanianShilling", "Tanzanian shilling"));
        locales.put("UAH", new Pair("ukrainianHryvnia", "Hryvnia"));
        locales.put("UGX", new Pair("ugandaShilling", "Uganda shilling"));
        locales.put("UYU", new Pair("uruguayanPeso", "Peso Uruguayo"));
        locales.put("VEF", new Pair("venezuelanBolívar", "Venezuelan bolívar fuerte"));
        locales.put("VUV", new Pair("vanuatuVatu", "Vanuatu vatu")); //++
        locales.put("WST", new Pair("samoanTala", "Samoan tala"));
        locales.put("XAF", new Pair("centralAfricanCfaFranc", "CFA franc BEAC"));
        locales.put("XCD", new Pair("eastCaribbeanDollar", "East Caribbean dollar"));
        locales.put("XOF", new Pair("westAfricanCfaFranc", "CFA Franc BCEAO"));
        locales.put("XPF", new Pair("cfpFranc", "CFP franc"));
        locales.put("YER", new Pair("yemeniRial", "Yemeni rial"));
        locales.put("ZAR", new Pair("southAfricanRand", "South African rand"));
        locales.put("ZMW", new Pair("zambianKwacha", "Zambian kwacha"));
        locales.put("ZWL", new Pair("zimbabweanDollar", "Zimbabwean dollar")); // wrong: "ZWD"
    }

    /**
     * Get the locale for a given currency code. This method is not accurate since various locales can share the same
     * currency.
     *
     * @param currencyCode Currency code such as "USD", "GBP", etc.
     * @return Corresponding locale for the currency.
     */
    public static Locale getLocaleForCurrency(String currencyCode) {
        Currency currency = Currency.getInstance(currencyCode);
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
            if (numberFormat.getCurrency() == currency) {
                return locale;
            }
        }

        return null;
    }

    /**
     * Show currency information for a locale.
     *
     * @param locale Locale configuration.
     */
    public static String displayCurrencyInfoForLocale(Locale locale) {
        StringBuilder sb = new StringBuilder();
        sb.append("Locale: " + locale.getDisplayName()).append("\n");
        Currency currency = Currency.getInstance(locale);
        sb.append("Currency Code: " + currency.getCurrencyCode()).append("\n");
        sb.append("Symbol: " + currency.getSymbol(locale)).append("\n");
        sb.append("Default Fraction Digits: " + currency.getDefaultFractionDigits()).append("\n");

        return sb.toString();
    }

}

