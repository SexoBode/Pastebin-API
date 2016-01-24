/*The MIT License (MIT)

Copyright (c) 2016 SexoBode (on GitHub)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.*/
package project.connection;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.locks.ReentrantLock;

//lock is not serializable, can it be used in the definition?
public class Paste extends PastebinRequest implements Serializable {
	private static final long serialVersionUID = -6215024748468223456L;
	
	private static final String OPTION = "&api_option=paste";
	private static final String TEXT = "&api_paste_code=";
	private static final String TITLE = "&api_paste_name=";
	private static final String FORMAT = "&api_paste_format=";
	private static final String PRIVACY = "&api_paste_private=";
	private static final String EXPIRATIONDATE = "&api_paste_expire_date=";
	
	private volatile String devKey = null;
	final ReentrantLock devKeyLock = new ReentrantLock();
	private volatile String text = null;
	private final ReentrantLock textLock = new ReentrantLock();
	private volatile String userKey = null;
	final ReentrantLock userKeyLock = new ReentrantLock();
	private volatile boolean useUserKey = false;
	final ReentrantLock usingUserKeyLock = new ReentrantLock();
	private volatile String title = null;
	private final ReentrantLock titleLock = new ReentrantLock();
	private volatile Format format = null;
	private final ReentrantLock formatLock = new ReentrantLock();
	private volatile Privacy privacy = null;
	private final ReentrantLock privacyLock = new ReentrantLock();
	private volatile Expiration expirationDate = null;
	private final ReentrantLock expirationLock = new ReentrantLock();
	
	public enum Format {
		_4CS("4cs"), _6502ACME("6502acme"),
		_6502KICK("6502kickass"), _6502TASM("6502tasm"),
		ABAP("abap"), ACTIONSCRIPT("actionscript"), ACTIONSCRIPT3("actionscript3"),
		ADA("ada"), AIMMS("aimms"), ALGOL68("algol68"), APACHE("apache"),
		APPLESCRIPT("applescript"), APT("apt_sources"), ARM("arm"),
		NASM("asm"), ASP("asp"), ASYMPTOTE("asymptote"), AUTOCONF("autoconf"),
		AUTOHOTKEY("autohotkey"), AUTOIT("autoit"), AVISYNTH("avisynth"),
		AWK("awk"), BASCOM("bascomavr"), BASH("bash"), BASIC4GL("basic4gl"),
		BATCH("dos"), BIBTEX("bibtex"), BLITZBASIC("blitzbasic"), BLITZ3D("b3d"),
		BLITZMAX("bmx"), BNF("bnf"), BOO("boo"), BRAINFUCK("bf"), C("c"),
		C_WIN("c_winapi"), C_MACS("c_mac"), C_INTERMEDIATE("cil"), CSHARP("csharp"),
		CPP("cpp"), CPP_WIN("cpp-winapi"), CPP_QT("cpp-qt"), C_LOADRUNNER("c_loadrunner"),
		CAD_DCL("caddcl"), CAD_LISP("cadlisp"), CFDG("cfdg"), CHAISCRIPT("chaiscript"),
		CHAPEL("chapel"), CLOJURE("clojure"), C_CLONE("klonec"), CPP_CLONE("klonecpp"),
		CMAKE("cmake"), COBOL("cobol"), COFFEESCRIPT("coffeescript"), COLDFUSION("cfm"),
		CSS("css"), CUESHEET("cuesheet"), D("d"), DART("dart"), DCL("dcl"),
		DCPU_16("dcpu16"), DCS("dcs"), DELPHI("delphi"), DELPHI_PRISM("oxygene"),
		DIFF("diff"), DIV("div"), DOT("dot"), E("e"), EASYTRIEVE("ezt"),
		ECMASCRIPT("ecmascript"), EIFFEL("eiffel"), EMAIL("email"), EPC("epc"),
		ERLANG("erlang"), FSHARP("fsharp"), FALCON("falcon"), FO_LANGUAGE("fo"),
		FORMULA_ONE("f1"), FORTRAN("fortran"), FREEBASIC("freebasic"),
		FREESWITCH("freeswitch"), GAMBAS("gambas"), GAME_MAKER("gml"), GDB("gdb"),
		GENERO("genero"), GENIE("genie"), GETTEXT("gettext"), GO("go"), GROOVY("groovy"),
		GWBASIC("gwbasic"), HASKELL("haskell"), HAXE("haxe"), HICEST("hicest"),
		HQ9PLUS("hq9plus"), HTML4("html4strict"), HTML5("html5"), ICON("icon"),
		IDL("idl"), INIFILE("ini"), INNOSCRIPT("inno"), INTERCAL("intercal"), IO("io"),
		ISPFPANEL("ispfpanel"), J("j"), JAVA("java"), JAVA5("java5"),
		JAVASCRIPT("javascript"), JCL("jcl"), JQUERY("jquery"), JSON("json"),
		JULIA("julia"), KIXTART("kixtart"), LATEX("latex"), LDIF("ldif"),
		LIBERTYBASIC("lb"), LINDENSCRIPT("lsl2"), LISP("lisp"), LLVM("llvm"),
		LOCOBASIC("locobasic"), LOGTALK("logtalk"), LOLCODE("lolcode"),
		LOTUSFORMULAS("lotusformulas"), LOTUSSCRIPT("lotusscript"), LSCRIPT("lscript"),
		LUA("lua"), MK68000("m68k"), MAGIKSF("magiksf"), MAKE("make"),
		MAPBASIC("mapbasic"), MATLAB("matlab"), MIRC("mirc"), MIX("mmix"),
		MODULA2("modula2"), MODULA3("modula3"), MOTOROLA68000_HISOFT_DEV("68000devpac"),
		MPASM("mpasm"), MXML("mxml"), MYSQL("mysql"), NAGIOS("nagios"),
		NETREXX("netrexx"), NEWLISP("newlisp"), NGINX("nginx"), NIMROD("nimrod"),
		NONE("text"), NULLSOFT_INSTALLER("nsis"), OBERON2("oberon2"), OBJECK("objeck"),
		OBJECTIVE_C("objc"), OCAML_BRIEF("ocaml-brief"), OCAML("ocaml"), OCTAVE("octave"),
		OPENBSD_PACKET_FILTER("pf"), OPENGL_SHADING("glsl"), OPENOFFICEBASIC("oobas"),
		ORACLE11("oracle11"), ORACLE8("oracle8"), OZ("oz"), PARASAIL("parasail"),
		PARIGP("parigp"), PASCAL("pascal"), PAWN("pawn"), PCRE("pcre"), PER("per"),
		PERL("perl"), PERL6("perl6"), PHP("php"), PHP_BRIEF("php-brief"), PIC16("pic16"),
		PIKE("pike"), PIXEL_BENDER("pixelbender"), PLSQL("plsql"),
		POSTGRESQL("postgresql"), POSTSCRIPT("postscript"), POVRAY("povray"),
		POWERSHELL("powershell"), POWERBUILDER("powerbuilder"), PROFTPD("proftpd"),
		PROGRESS("progress"), PROLOG("prolog"), PROPERTIES("properties"),
		PROVIDEX("providex"), PUPPET("puppet"), PUREBASIC("purebasic"), PYCON("pycon"),
		PYTHON("python"), PYTHONS60("pys60"), Q_KDB("q"), QBASIC("qbasic"), QML("qml"),
		R("rsplus"), RACKET("racket"), RAILS("rails"), RBSCRIPT("rbs"), REBOL("rebol"),
		REG("reg"), REXX("rexx"), ROBOTS("robots"), RPM_SPEC("rpmspec"), RUBY("ruby"),
		RUBY_GNUPLOT("gnuplot"), RUST("rust"), SAS("sas"), SCALA("scala"), SCHEME("scheme"),
		SCILAB("scilab"), SCL("scl"), SDLBASIC("sdlbasic"), SMALLTALK("smalltalk"),
		SMARTY("smarty"), SPARK("spark"), SPARQL("sparql"), SQF("sqf"), SQL("sql"),
		STANDARDML("standardml"), STONESCRIPT("stonescript"), SUPERCOLLIDER("sclang"),
		SWIFT("swift"), SYSTEMVERILOG("systemverilog"), T_SQL("tsql"), TCL("tcl"),
		TERATERM("teraterm"), THINBASIC("thinbasic"), TYPOSCRIPT("typoscript"),
		UNICON("unicon"), UNREALSCRIPT("uscript"), UPC("ups"), URBI("urbi"), VALA("vala"),
		VBNET("vbnet"), VBSCRIPT("vbscript"), VEDIT("vedit"), VERILOG("verilog"),
		VHDL("vhdl"), VIM("vim"), VISUALPROLOG("visualprolog"), VISUALBASIC("vb"),
		VISUALFOXPRO("visualfoxpro"), WHITESPACE("whitespace"), WHOIS("whois"),
		WINBATCH("winbatch"), XBASIC("xbasic"), XML("xml"), XORG_CONFIG("xorg_conf"),
		XPP("xpp"), YAML("yaml"), Z80("z80"), ZXBASIC("zxbasic");
		
		private String format;
		
		private Format(String format) {
			this.format = format;
		}
		
		@Override
		public String toString() {
			return format;
		}
	}
	
	public enum Privacy {
		PUBLIC("0"), UNLISTED("1"), PRIVATE("2");
		
		private String privacy;
		
		private Privacy(String privacy) {
			this.privacy = privacy;
		}
		
		@Override
		public String toString() {
			return privacy;
		}
	}
	
	public enum Expiration {
		NEVER("N"), MINS_10("10M"), HOURS_1("1H"),
		DAYS_1("1D"), WEEKS_1("1W"), WEEKS_2("2W"), MONTHS_1("1M");
		
		private String expiration;
		
		private Expiration(String expiration) {
			this.expiration = expiration;
		}
		
		@Override
		public String toString() {
			return expiration;
		}
	}
	
	public Paste() {}
	
	public Paste(String text) {
		this.text(text);
	}
	
	private Paste(Paste toCopy) {
		textLock.lock();
		usingUserKeyLock.lock();
		titleLock.lock();
		formatLock.lock();
		privacyLock.lock();
		expirationLock.lock();
		
		this.text = toCopy.text;
		this.useUserKey = toCopy.useUserKey;
		this.title = toCopy.title;
		this.format = toCopy.format;
		this.privacy = toCopy.privacy;
		this.expirationDate = toCopy.expirationDate;
		
		expirationLock.unlock();
		privacyLock.unlock();
		formatLock.unlock();
		titleLock.unlock();
		usingUserKeyLock.unlock();
		textLock.unlock();
	}
	
	public Paste copy() {
		return new Paste(this);
	}
	
	void setDevKey(String devKey) {
		devKeyLock.lock();
		
		this.devKey = devKey;
		
		devKeyLock.unlock();
	}
	
	public Paste text(String text) {
		textLock.lock();
		
		if(text == null || text.equals("")) {
			throw new NullPointerException("Text must not be null nor empty.");
		}
		
		this.text = text;
		
		textLock.unlock();
		return this;
	}
	
	public String getText() {
		return text;
	}
	
	public Paste asGuest() {
		usingUserKeyLock.lock();
		
		useUserKey = false;
	
		usingUserKeyLock.unlock();
		return this;
	}
	
	public Paste asUser() {
		usingUserKeyLock.lock();
		
		useUserKey = true;
		
		usingUserKeyLock.unlock();
		return this;
	}
	
	void setUserKey(String userKey) {
		userKeyLock.lock();
		
		this.userKey = userKey;
		
		userKeyLock.unlock();
	}
	
	public boolean isUsingUserKey() {
		return useUserKey;
	}

	public Paste title(String title) {
		titleLock.lock();
		
		this.title = title;
		
		titleLock.unlock();
		return this;
	}
	
	public String getTitle() {
		return title;
	}
	
	public Paste format(Format format) {
		formatLock.lock();
		
		this.format = format;
		
		formatLock.unlock();
		return this;
	}
	
	public Format getFormat() {
		return format;
	}
	
	public Paste privacy(Privacy privacy) {
		privacyLock.lock();
		
		this.privacy = privacy;
		
		privacyLock.unlock();
		return this;
	}
	
	public Privacy getPrivacy() {
		return privacy;
	}
	
	public Paste expirationDate(Expiration expiration) {
		expirationLock.lock();
		
		this.expirationDate = expiration;
		
		expirationLock.unlock();
		return this;
	}

	public Expiration getExpirationDate() {
		return expirationDate;
	}

	@Override
	String asPOST() {
		devKeyLock.lock();
		textLock.lock();
		userKeyLock.lock();
		titleLock.lock();
		formatLock.lock();
		privacyLock.lock();
		expirationLock.lock();
		
		final StringBuilder unfinished;
		
		try {
			unfinished = new StringBuilder(DEVKEY + URLEncoder.encode(devKey, "UTF-8") +
					OPTION + TEXT + URLEncoder.encode(text, "UTF-8"));
			
			if(userKey != null) {
				unfinished.append(USERKEY).append(URLEncoder.encode(userKey, "UTF-8"));
			}
			
			if(title != null) {
				unfinished.append(TITLE).append(URLEncoder.encode(title, "UTF-8"));
			}
			
			if(format != null) {
				unfinished.append(FORMAT).append(URLEncoder.encode(format.toString(), "UTF-8"));
			}
			
			if(privacy != null) {
				if(privacy == Privacy.PRIVATE && userKey == null) {
					throw new BadPasteException("Can't make a private paste without being logged in.");
				}
				
				unfinished.append(PRIVACY).append(URLEncoder.encode(privacy.toString(), "UTF-8"));
			}
			
			if(expirationDate != null) {
				unfinished.append(EXPIRATIONDATE).append(URLEncoder.encode(expirationDate.toString(), "UTF-8"));
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("This should not happen unless the encoding is incorrect (meaning the programmer made a mistake).", e);
		} finally {
			expirationLock.unlock();
			privacyLock.unlock();
			formatLock.unlock();
			titleLock.unlock();
			userKeyLock.unlock();
			textLock.unlock();
			devKeyLock.unlock();
		}
		
		return unfinished.toString();
	}
	
}
