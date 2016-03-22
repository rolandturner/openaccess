
/*
 * Copyright (c) 1998 - 2005 Versant Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Versant Corporation - initial API and implementation
 */
package com.versant.core.jdbc.sql;

import java.util.HashMap;
import java.util.HashSet;

import com.versant.core.common.BindingSupportImpl;

/**
 * The standard name generator. This tries to make sure that generated names
 * will be valid for all databases. This ensures that applications deployed
 * against different databases maintain the same schema.
 */
public class DefaultJdbcNameGenerator implements JdbcNameGenerator {

    protected String databaseType;

    protected int maxTableNameLength = 18;
    protected int maxColumnNameLength = 18;
    protected int maxConstraintNameLength = 18;
    protected int maxIndexNameLength = 18;
    protected String pkSuffix = "_id";
    protected String pkConstraintPrefix = "pk_";
    protected String refConstraintPrefix = "ref_";
    protected String wordBreak = "_";
    protected String sequenceColumnName = "seq";
    protected String valueColumnName = "val";
    protected String keyColumnName = "mapkey";

	protected String classIdColumnName = "jdo_class";

 
	protected String polyRefClassIdSuffix = "_class";
    protected String indexNamePrefix = "idx_";

    private boolean nameCanStartWithUnderscore = true;

    protected HashMap reservedWords = new HashMap();

    /**
     * This maps a table name to a HashSet of its column names.
     */
    protected HashMap tableMap = new HashMap();
    /**
     * All the constraint names we know about.
     */
    protected HashSet constraintNames = new HashSet();
    /**
     * All the index names we know about.
     */
    protected HashSet indexNames = new HashSet();

    public DefaultJdbcNameGenerator() {
        populateReservedWords(reservedWords);
    }

    /**
     * This is called immediately after the name generator has been constructed
     * to indicate which database is in use.
     */
    public void setDatabaseType(String db) {
        databaseType = db;
        if (db.equals("daffodil")) {
            reservedWords.put("connection", "connectn");
            reservedWords.put("recursive", "recursve");
            reservedWords.put("timestamp", "timestmp");
            reservedWords.put("interval", "intervl");
            reservedWords.put("email", "eml");
            reservedWords.put("action", "actn");
        } else if (db.equals("mckoi")) {
            reservedWords.put("sequence", "seqnce");
        } else if (db.equals("mssql")) {
            reservedWords.put("system_user", "sys_user");
            reservedWords.put("identity", "ident");
        } else if (db.equals("sapdb")) {
            reservedWords.put("timestamp", "timestmp");
        } else if (db.equals("firebird") || db.equals("interbase")) {
            reservedWords.put("position", "positn");
            reservedWords.put("admin", "admn");
            reservedWords.put("timestamp", "timestmp");
            reservedWords.put("action", "actn");
        } else if (db.equals("informix") || db.equals("informixse")) {
            reservedWords.put("interval", "intervl");
        } else if (db.equals("mysql")) {
            reservedWords.put("interval", "intervl");
            reservedWords.put("change", "chnge");
            reservedWords.put("keys", "kys");
            reservedWords.put("xor", "xr");
        } else if (db.equals("cache")) {
            reservedWords.put("connection", "connectn");
            reservedWords.put("timestamp", "timestmp");
            reservedWords.put("interval", "intrval");
            reservedWords.put("action", "actn");
        } else if (db.equals("hypersonic")) {
            reservedWords.put("position", "pos");
        } else if (db.equals("oracle")) {
            nameCanStartWithUnderscore = false;
        }
    }

    /**
     * Populate rw with all reserved words and their replacements.
     * Any identifiers that match a reserved word will be replaced.
     * All keys must be lower case.
     */
    protected void populateReservedWords(HashMap rw) {
        rw.put("abort", "abrt");
        rw.put("abs", "ab");
        rw.put("absolute", "abslute");
        rw.put("accept", "accpt");
        rw.put("acces", "accs");
        rw.put("access", "accss");
        rw.put("activate", "actvate");
        rw.put("add", "ad");
        rw.put("addform", "addfrm");
        rw.put("after", "aftr");
        rw.put("all", "al");
        rw.put("alter", "altr");
        rw.put("and", "nd");
        rw.put("andfilename", "andflename");
        rw.put("any", "ny");
        rw.put("anyfinish", "anyfnish");
        rw.put("append", "appnd");
        rw.put("archive", "archve");
        rw.put("array", "arry");
        rw.put("as", "asx");
        rw.put("asc", "sc");
        rw.put("ascending", "ascnding");
        rw.put("ascii", "asci");
        rw.put("assert", "assrt");
        rw.put("assign", "assgn");
        rw.put("at", "atx");
        rw.put("attribute", "attrbute");
        rw.put("attributes", "attrbutes");
        rw.put("audit", "audt");
        rw.put("authorization", "athorization");
        rw.put("autonext", "atonext");
        rw.put("average", "avrage");
        rw.put("avg", "av");
        rw.put("avgu", "avgup");
        rw.put("backout", "bckout");
        rw.put("before", "bfore");
        rw.put("begin", "bgin");
        rw.put("beginload", "bginload");
        rw.put("beginmodify", "bginmodify");
        rw.put("beginning", "bginning");
        rw.put("begwork", "bgwork");
        rw.put("between", "btween");
        rw.put("betweenby", "btweenby");
        rw.put("border", "brder");
        rw.put("bottom", "bttom");
        rw.put("break", "brak");
        rw.put("breakdisplay", "brakdisplay");
        rw.put("browse", "brwse");
        rw.put("bufered", "bfered");
        rw.put("buffer", "bffer");
        rw.put("buffered", "bffered");
        rw.put("bulk", "blk");
        rw.put("by", "byx");
        rw.put("byte", "byt");
        rw.put("call", "cll");
        rw.put("cancel", "cncel");
        rw.put("cascade", "cscade");
        rw.put("case", "cse");
        rw.put("change", "chnge");
        rw.put("char", "chr");
        rw.put("char_convert", "chr_convert");
        rw.put("character", "chracter");
        rw.put("check", "chck");
        rw.put("checkpoint", "chckpoint");
        rw.put("chr2fl", "chr2f");
        rw.put("chr2flo", "chr2flt");
        rw.put("chr2floa", "chr2fla");
        rw.put("chr2float", "chr2flat");
        rw.put("chr2int", "chr2nt");
        rw.put("clear", "clar");
        rw.put("clearrow", "clarrow");
        rw.put("clipped", "clpped");
        rw.put("close", "clse");
        rw.put("cluster", "clustr");
        rw.put("clustered", "clstered");
        rw.put("clustering", "clstering");
        rw.put("cobol", "cbol");
        rw.put("cold", "cld");
        rw.put("column", "colmn");
        rw.put("columns", "clumns");
        rw.put("command", "cmmand");
        rw.put("comment", "commnt");
        rw.put("commit", "cmmit");
        rw.put("committed", "cmmitted");
        rw.put("compress", "comprss");
        rw.put("compute", "cmpute");
        rw.put("concat", "cncat");
        rw.put("cond", "cnd");
        rw.put("config", "cnfig");
        rw.put("confirm", "cnfirm");
        rw.put("connect", "connct");
        rw.put("constraint", "cnstraint");
        rw.put("construct", "cnstruct");
        rw.put("contain", "cntain");
        rw.put("contains", "cntains");
        rw.put("continue", "cntinue");
        rw.put("controlrow", "cntrolrow");
        rw.put("convert", "cnvert");
        rw.put("copy", "cpy");
        rw.put("count", "cnt");
        rw.put("countu", "cntu");
        rw.put("countucreate", "cuntucreate");
        rw.put("crash", "crsh");
        rw.put("create", "creat");
        rw.put("current", "currnt");
        rw.put("cursor", "crsor");
        rw.put("data", "dta");
        rw.put("data_pgs", "dta_pgs");
        rw.put("database", "dtabase");
        rw.put("datapages", "dtapages");
        rw.put("date", "dte");
        rw.put("day", "dy");
        rw.put("daynum", "dynum");
        rw.put("dba", "dbax");
        rw.put("dbcc", "dbc");
        rw.put("dbe", "dbex");
        rw.put("dbefile", "dbfile");
        rw.put("dbefileo", "dbfileo");
        rw.put("dbefileset", "dbfileset");
        rw.put("dbspace", "dbspce");
        rw.put("dbyte", "dbyt");
        rw.put("dec", "dc");
        rw.put("decending", "dcending");
        rw.put("decimal", "deciml");
        rw.put("declare", "dclare");
        rw.put("default", "deflt");
        rw.put("defaults", "dfaults");
        rw.put("defer", "dfer");
        rw.put("define", "dfine");
        rw.put("definition", "dfinition");
        rw.put("delete", "delte");
        rw.put("deleterow", "dleterow");
        rw.put("desc", "dsc");
        rw.put("descending", "dscending");
        rw.put("descendng", "dscendng");
        rw.put("describe", "dscribe");
        rw.put("descriptor", "dscriptor");
        rw.put("destpos", "dstpos");
        rw.put("destroy", "dstroy");
        rw.put("device", "dvice");
        rw.put("devspace", "dvspace");
        rw.put("direct", "drect");
        rw.put("dirty", "drty");
        rw.put("disconnect", "dsconnect");
        rw.put("disk", "dsk");
        rw.put("displace", "dsplace");
        rw.put("display", "dsplay");
        rw.put("distinct", "distnct");
        rw.put("distribution", "dstribution");
        rw.put("div", "dv");
        rw.put("do", "d");
        rw.put("does", "des");
        rw.put("domain", "dmain");
        rw.put("double", "duble");
        rw.put("down", "dwn");
        rw.put("drop", "drp");
        rw.put("dual", "dal");
        rw.put("dummy", "dmmy");
        rw.put("dump", "dmp");
        rw.put("duplicates", "dplicates");
        rw.put("each", "ech");
        rw.put("ebcdic", "ebcdc");
        rw.put("ed_string", "ed_strng");
        rw.put("editadd", "edtadd");
        rw.put("editupdate", "edtupdate");
        rw.put("else", "els");
        rw.put("elseif", "elsif");
        rw.put("end", "en");
        rw.put("end_error", "end_rror");
        rw.put("end_fetch", "end_ftch");
        rw.put("end_for", "end_fr");
        rw.put("end_get", "end_gt");
        rw.put("end_modify", "end_mdify");
        rw.put("end_place", "end_plce");
        rw.put("end_segment_s", "end_sgment_s");
        rw.put("end_segment_string", "end_sgment_string");
        rw.put("end_store", "end_stre");
        rw.put("end_stream", "end_stram");
        rw.put("enddata", "enddta");
        rw.put("enddisplay", "enddsplay");
        rw.put("endforms", "endfrms");
        rw.put("endif", "endf");
        rw.put("ending", "endng");
        rw.put("endload", "endlad");
        rw.put("endloop", "endlop");
        rw.put("endmodify", "endmdify");
        rw.put("endpos", "endps");
        rw.put("endretrieve", "endrtrieve");
        rw.put("endselect", "endslect");
        rw.put("endwhile", "endwhle");
        rw.put("eq", "e");
        rw.put("erase", "erse");
        rw.put("errlvl", "errlv");
        rw.put("error", "errr");
        rw.put("errorexit", "errrexit");
        rw.put("evaluate", "evluate");
        rw.put("evaluating", "evluating");
        rw.put("every", "evry");
        rw.put("except", "excpt");
        rw.put("exclusive", "exclusve");
        rw.put("exec", "exc");
        rw.put("execute", "excute");
        rw.put("exists", "exsts");
        rw.put("exit", "ext");
        rw.put("explicit", "explcit");
        rw.put("extent", "extnt");
        rw.put("external", "extrnal");
        rw.put("false", "flse");
        rw.put("fetch", "ftch");
        rw.put("field", "feld");
        rw.put("file", "fle");
        rw.put("filename", "flename");
        rw.put("fillfactor", "fllfactor");
        rw.put("finalise", "fnalise");
        rw.put("finalize", "fnalize");
        rw.put("findstr", "fndstr");
        rw.put("finish", "fnish");
        rw.put("first", "frst");
        rw.put("firstpos", "frstpos");
        rw.put("fixed", "fxed");
        rw.put("fl", "flx");
        rw.put("float", "flt");
        rw.put("flush", "flsh");
        rw.put("for", "fr");
        rw.put("foreach", "freach");
        rw.put("format", "frmat");
        rw.put("formdata", "frmdata");
        rw.put("forminit", "frminit");
        rw.put("forms", "frms");
        rw.put("fortran", "frtran");
        rw.put("found", "foundx");
        rw.put("frant", "frnt");
        rw.put("fraphic", "frphic");
        rw.put("free", "fre");
        rw.put("from", "frm");
        rw.put("frs", "frss");
        rw.put("function", "fnction");
        rw.put("get", "gett");
        rw.put("getform", "gtform");
        rw.put("getoper", "gtoper");
        rw.put("getrow", "gtrow");
        rw.put("global", "glbal");
        rw.put("globals", "glbals");
        rw.put("go", "gox");
        rw.put("goto", "gotox");
        rw.put("grant", "grnt");
        rw.put("graphic", "grphic");
        rw.put("group", "grp");
        rw.put("gt", "gtx");
        rw.put("having", "havng");
        rw.put("header", "hader");
        rw.put("help", "hlp");
        rw.put("help_frs", "hlp_frs");
        rw.put("helpfile", "hlpfile");
        rw.put("hold", "hld");
        rw.put("holdlock", "hldlock");
        rw.put("identified", "identifd");
        rw.put("identifield", "idntifield");
        rw.put("if", "ifx");
        rw.put("ifdef", "ifdf");
        rw.put("ignore", "ignre");
        rw.put("image", "imge");
        rw.put("immediate", "immediat");
        rw.put("immidiate", "immidiat");
        rw.put("implicit", "implcit");
        rw.put("in", "inx");
        rw.put("include", "inclde");
        rw.put("increment", "incremnt");
        rw.put("index", "indx");
        rw.put("indexed", "indxed");
        rw.put("indexname", "indxname");
        rw.put("indexpages", "indxpages");
        rw.put("indicator", "indcator");
        rw.put("infield", "infeld");
        rw.put("info", "inf");
        rw.put("ingres", "ingrs");
        rw.put("init", "initt");
        rw.put("initial", "initl");
        rw.put("initialise", "intialise");
        rw.put("initialize", "intialize");
        rw.put("inittable", "inttable");
        rw.put("input", "inpt");
        rw.put("inquir_frs", "inqir_frs");
        rw.put("inquire_equel", "inqire_equel");
        rw.put("inquire_frs", "inqire_frs");
        rw.put("inquire_ingres", "inqire_ingres");
        rw.put("insert", "insrt");
        rw.put("insertrow", "insrtrow");
        rw.put("instructions", "instrctions");
        rw.put("int", "inte");
        rw.put("int2chr", "int2ch");
        rw.put("integer", "integr");
        rw.put("integrity", "intgrity");
        rw.put("interesect", "intresect");
        rw.put("interrupt", "intrrupt");
        rw.put("intersect", "intersct");
        rw.put("into", "intox");
        rw.put("intschr", "intsch");
        rw.put("invoke", "invke");
        rw.put("is", "isx");
        rw.put("isam", "ism");
        rw.put("isolation", "islation");
        rw.put("journaling", "jurnaling");
        rw.put("key", "ky");
        rw.put("kill", "kll");
        rw.put("label", "lbel");
        rw.put("language", "lnguage");
        rw.put("last", "lastx");
        rw.put("lastpos", "lstpos");
        rw.put("le", "lex");
        rw.put("left", "lft");
        rw.put("length", "lngth");
        rw.put("lenstr", "lnstr");
        rw.put("let", "lett");
        rw.put("level", "lvl");
        rw.put("like", "lke");
        rw.put("likeproceduretp", "lkeproceduretp");
        rw.put("line", "lne");
        rw.put("lineno", "lneno");
        rw.put("lines", "lnes");
        rw.put("link", "lnk");
        rw.put("list", "lst");
        rw.put("load", "ld");
        rw.put("loadtable", "ldtable");
        rw.put("loadtableresume", "ldtableresume");
        rw.put("local", "lcal");
        rw.put("location", "lcation");
        rw.put("lock", "lck");
        rw.put("locking", "lcking");
        rw.put("log", "lg");
        rw.put("long", "lng");
        rw.put("lower", "lwer");
        rw.put("lpad", "lpd");
        rw.put("lt", "ltx");
        rw.put("main", "mainx");
        rw.put("manual", "mnual");
        rw.put("manuitem", "manitem");
        rw.put("margin", "mrgin");
        rw.put("matches", "mtches");
        rw.put("matching", "mtching");
        rw.put("max", "mx");
        rw.put("maxextents", "maxextnts");
        rw.put("maxpublicunion", "mxpublicunion");
        rw.put("maxreclen", "mxreclen");
        rw.put("mdy", "mdyy");
        rw.put("menu", "mnu");
        rw.put("menuitem", "mnuitem");
        rw.put("menuitemscreen", "mnuitemscreen");
        rw.put("message", "msg");
        rw.put("messagerelocate", "msgrelocate");
        rw.put("messagescroll", "msgscroll");
        rw.put("mfetch", "mftch");
        rw.put("min", "minx");
        rw.put("minreclen", "mnreclen");
        rw.put("minreturnuntil", "mnreturnuntil");
        rw.put("minus", "mnus");
        rw.put("mirrorexit", "mrrorexit");
        rw.put("missing", "mssing");
        rw.put("mixed", "mxed");
        rw.put("mlslabel", "mlslabl");
        rw.put("mod", "modd");
        rw.put("mode", "mde");
        rw.put("modify", "modfy");
        rw.put("modifyrevokeupdat", "mdifyrevokeupdat");
        rw.put("module", "mdule");
        rw.put("money", "mney");
        rw.put("monitor", "mnitor");
        rw.put("month", "mnth");
        rw.put("move", "mve");
        rw.put("multi", "mlti");
        rw.put("name", "nme");
        rw.put("ne", "nex");
        rw.put("need", "ned");
        rw.put("new", "nw");
        rw.put("newlog", "nwlog");
        rw.put("next", "nxt");
        rw.put("nextscrolldown", "nxtscrolldown");
        rw.put("no", "nox");
        rw.put("noaudit", "noaudt");
        rw.put("nocompress", "nocomprss");
        rw.put("nocr", "ncr");
        rw.put("nojournaling", "njournaling");
        rw.put("nolist", "nlist");
        rw.put("nolog", "nlog");
        rw.put("nonclustered", "nnclustered");
        rw.put("normal", "nrmal");
        rw.put("nosyssort", "nsyssort");
        rw.put("not", "nt");
        rw.put("notffound", "ntffound");
        rw.put("notfound", "ntfound");
        rw.put("notrans", "ntrans");
        rw.put("notrim", "ntrim");
        rw.put("notrimscrollup", "ntrimscrollup");
        rw.put("notrollbackuser", "ntrollbackuser");
        rw.put("nowait", "nowt");
        rw.put("null", "nll");
        rw.put("nullify", "nllify");
        rw.put("nullsaveusing", "nllsaveusing");
        rw.put("nullval", "nllval");
        rw.put("num", "numbr");
        rw.put("number", "numbr");
        rw.put("numeric", "numerc");
        rw.put("nxfield", "nxfeld");
        rw.put("of", "ofx");
        rw.put("off", "offx");
        rw.put("offline", "offln");
        rw.put("offsets", "offsts");
        rw.put("ofsavepointvalues", "ofsvepointvalues");
        rw.put("old", "oldd");
        rw.put("on", "onx");
        rw.put("once", "onc");
        rw.put("online", "onln");
        rw.put("onselectwhere", "onslectwhere");
        rw.put("onto", "ont");
        rw.put("open", "opn");
        rw.put("opensetwhile", "opnsetwhile");
        rw.put("opensleep", "opnsleep");
        rw.put("optimize", "optmize");
        rw.put("option", "optn");
        rw.put("options", "optons");
        rw.put("or", "orx");
        rw.put("order", "ordr");
        rw.put("ordersqlwork", "ordrsqlwork");
        rw.put("orsomewith", "orsmewith");
        rw.put("orsort", "orsrt");
        rw.put("otherwise", "othrwise");
        rw.put("out", "ot");
        rw.put("outer", "outerx");
        rw.put("output", "outputx");
        rw.put("output page", "otput page");
        rw.put("outstop", "otstop");
        rw.put("over", "ovr");
        rw.put("owner", "ownr");
        rw.put("ownership", "ownrship");
        rw.put("page", "pge");
        rw.put("pageno", "pgeno");
        rw.put("pages", "pges");
        rw.put("param", "parm");
        rw.put("partition", "prtition");
        rw.put("pascal", "pscal");
        rw.put("password", "passwd");
        rw.put("pathname", "pthname");
        rw.put("pattern", "pttern");
        rw.put("pause", "puse");
        rw.put("pctfree", "pctfr");
        rw.put("percent", "prcent");
        rw.put("perm", "prm");
        rw.put("permanent", "prmanent");
        rw.put("permit", "prmit");
        rw.put("permitsum", "prmitsum");
        rw.put("pipe", "ppe");
        rw.put("place", "plce");
        rw.put("plan", "pln");
        rw.put("pli", "pl");
        rw.put("pos", "ps");
        rw.put("power", "pwer");
        rw.put("precision", "prcision");
        rw.put("prepare", "prpare");
        rw.put("preparetable", "prparetable");
        rw.put("preserve", "prserve");
        rw.put("prev", "prv");
        rw.put("previous", "prvious");
        rw.put("prevision", "prvision");
        rw.put("print", "prnt");
        rw.put("printer", "prnter");
        rw.put("printscreen", "prntscreen");
        rw.put("printscreenscroll", "prntscreenscroll");
        rw.put("printsubmenu", "prntsubmenu");
        rw.put("printsumu", "prntsumu");
        rw.put("prior", "prr");
        rw.put("priv", "privx");
        rw.put("private", "prvate");
        rw.put("privilages", "prvilages");
        rw.put("privilagesthen", "prvilagesthen");
        rw.put("privileges", "privilgs");
        rw.put("proc", "prc");
        rw.put("procedure", "prcedure");
        rw.put("processexit", "prcessexit");
        rw.put("program", "prgram");
        rw.put("progusage", "prgusage");
        rw.put("prompt", "prmpt");
        rw.put("promptscrolldown", "prmptscrolldown");
        rw.put("prompttabledata", "prmpttabledata");
        rw.put("protect", "prtect");
        rw.put("psect", "psct");
        rw.put("public", "publc");
        rw.put("publicread", "pblicread");
        rw.put("put", "pt");
        rw.put("putform", "ptform");
        rw.put("putformscrollup", "ptformscrollup");
        rw.put("putformunloadtab", "ptformunloadtab");
        rw.put("putoper", "ptoper");
        rw.put("putopersleep", "ptopersleep");
        rw.put("putrow", "ptrow");
        rw.put("putrowsubmenu", "ptrowsubmenu");
        rw.put("putrowup", "ptrowup");
        rw.put("query", "qery");
        rw.put("quick", "qick");
        rw.put("quit", "qit");
        rw.put("raiserror", "riserror");
        rw.put("range", "rnge");
        rw.put("rangeto", "rngeto");
        rw.put("raw", "rawx");
        rw.put("rdb$db_key", "rdb$db_ky");
        rw.put("rdb$length", "rdb$lngth");
        rw.put("rdb$missing", "rdb$mssing");
        rw.put("rdb$value", "rdb$vlue");
        rw.put("rdb4db_key", "rdb4db_ky");
        rw.put("rdb4length", "rdb4lngth");
        rw.put("rdb4missing", "rdb4mssing");
        rw.put("rdb4value", "rdb4vlue");
        rw.put("read", "rad");
        rw.put("read_only", "rd_only");
        rw.put("read_write", "rd_write");
        rw.put("readonly", "rdonly");
        rw.put("readpass", "rdpass");
        rw.put("readtext", "rdtext");
        rw.put("readwrite", "rdwrite");
        rw.put("ready", "rdy");
        rw.put("real", "rl");
        rw.put("reconfigure", "rconfigure");
        rw.put("reconnect", "rconnect");
        rw.put("record", "rcord");
        rw.put("recover", "rcover");
        rw.put("redisplay", "rdisplay");
        rw.put("redisplaytabledata", "rdisplaytabledata");
        rw.put("redisplayvalidate", "rdisplayvalidate");
        rw.put("reduced", "rduced");
        rw.put("register", "rgister");
        rw.put("registerunloaddata", "rgisterunloaddata");
        rw.put("registervalidrow", "rgistervalidrow");
        rw.put("reject", "rject");
        rw.put("relative", "rlative");
        rw.put("release", "rlease");
        rw.put("reload", "rload");
        rw.put("relocate", "rlocate");
        rw.put("relocateunique", "rlocateunique");
        rw.put("remove", "rmove");
        rw.put("removeuprelocatev", "rmoveuprelocatev");
        rw.put("removevalidate", "rmovevalidate");
        rw.put("removewhenever", "rmovewhenever");
        rw.put("rename", "renme");
        rw.put("repeat", "rpeat");
        rw.put("repeatable", "rpeatable");
        rw.put("repeated", "rpeated");
        rw.put("repeatvalidrow", "rpeatvalidrow");
        rw.put("replace", "rplace");
        rw.put("replaceuntil", "rplaceuntil");
        rw.put("replstr", "rplstr");
        rw.put("report", "rport");
        rw.put("request_handle", "rquest_handle");
        rw.put("reserved_pgs", "rserved_pgs");
        rw.put("reserving", "rserving");
        rw.put("reset", "rset");
        rw.put("resource", "resrce");
        rw.put("rest", "rst");
        rw.put("restart", "rstart");
        rw.put("restore", "rstore");
        rw.put("restrict", "rstrict");
        rw.put("resume", "rsume");
        rw.put("retrieve", "rtrieve");
        rw.put("retrieveupdate", "rtrieveupdate");
        rw.put("return", "rturn");
        rw.put("returning", "rturning");
        rw.put("revoke", "revke");
        rw.put("right", "rght");
        rw.put("role", "rle");
        rw.put("rollback", "rllback");
        rw.put("rollforward", "rllforward");
        rw.put("rololback", "rlolback");
        rw.put("round", "rund");
        rw.put("row", "rowx");
        rw.put("rowcnt", "rwcnt");
        rw.put("rowcount", "rwcount");
        rw.put("rowid", "rwid");
        rw.put("rownum", "rwnum");
        rw.put("rows", "rws");
        rw.put("rpad", "rpd");
        rw.put("rule", "rle");
        rw.put("run", "rn");
        rw.put("runtime", "rntime");
        rw.put("samplstdev", "smplstdev");
        rw.put("save", "sve");
        rw.put("savepoint", "svepoint");
        rw.put("savepointwhere", "svepointwhere");
        rw.put("saveview", "sveview");
        rw.put("schema", "schma");
        rw.put("scope", "scpe");
        rw.put("screen", "scren");
        rw.put("scroll", "scrll");
        rw.put("scrolldown", "scrlldown");
        rw.put("scrollup", "scrllup");
        rw.put("search", "sarch");
        rw.put("segment", "sgment");
        rw.put("sel", "sl");
        rw.put("sele", "sle");
        rw.put("selec", "slec");
        rw.put("select", "selct");
        rw.put("selupd", "slupd");
        rw.put("serial", "srial");
        rw.put("session", "sessn");
        rw.put("set", "st");
        rw.put("set_equel", "st_equel");
        rw.put("set_frs", "st_frs");
        rw.put("set_ingres", "st_ingres");
        rw.put("setuser", "stuser");
        rw.put("setwith", "stwith");
        rw.put("share", "shre");
        rw.put("shared", "shred");
        rw.put("short", "shrt");
        rw.put("show", "shw");
        rw.put("shutdown", "shtdown");
        rw.put("size", "sze");
        rw.put("skip", "skp");
        rw.put("sleep", "slep");
        rw.put("smallfloat", "smllfloat");
        rw.put("smallint", "smallnt");
        rw.put("some", "sme");
        rw.put("sort", "srt");
        rw.put("sorterd", "srterd");
        rw.put("sounds", "sunds");
        rw.put("sourcepos", "surcepos");
        rw.put("space", "spce");
        rw.put("spaces", "spces");
        rw.put("sql", "sq");
        rw.put("sqlcode", "sqlcde");
        rw.put("sqlda", "sqld");
        rw.put("sqlerror", "sqlrror");
        rw.put("sqlexeption", "sqlxeption");
        rw.put("sqlexplain", "sqlxplain");
        rw.put("sqlnotfound", "sqlntfound");
        rw.put("sqrt", "sqr");
        rw.put("stability", "stbility");
        rw.put("start", "strt");
        rw.put("start_segment", "strt_segment");
        rw.put("start_segmented_?", "strt_segmented");
        rw.put("start_stream", "strt_stream");
        rw.put("start_transaction", "strt_transaction");
        rw.put("starting", "strting");
        rw.put("startpos", "strtpos");
        rw.put("state", "stte");
        rw.put("statistics", "sttistics");
        rw.put("stdev", "stdv");
        rw.put("step", "stepx");
        rw.put("stop", "stopx");
        rw.put("store", "stre");
        rw.put("string", "strng");
        rw.put("submenu", "sbmenu");
        rw.put("substr", "sbstr");
        rw.put("succesfull", "sccesfull");
        rw.put("successful", "successfl");
        rw.put("successfull", "sccessfull");
        rw.put("sum", "sm");
        rw.put("sumu", "smu");
        rw.put("superdba", "sperdba");
        rw.put("syb_terminate", "syb_trminate");
        rw.put("synonym", "synonm");
        rw.put("sysdate", "sysdte");
        rw.put("syssort", "syssrt");
        rw.put("table", "tble");
        rw.put("tabledata", "tbledata");
        rw.put("temp", "tmp");
        rw.put("temporary", "tmporary");
        rw.put("terminate", "trminate");
        rw.put("text", "txt");
        rw.put("textsize", "txtsize");
        rw.put("then", "thn");
        rw.put("through", "thrugh");
        rw.put("thru", "thr");
        rw.put("tid", "td");
        rw.put("time", "tme");
        rw.put("to", "tox");
        rw.put("today", "tday");
        rw.put("tolower", "tlower");
        rw.put("top", "topp");
        rw.put("total", "ttal");
        rw.put("toupper", "tupper");
        rw.put("tp", "tpx");
        rw.put("trailer", "triler");
        rw.put("tran", "trn");
        rw.put("trans", "trns");
        rw.put("transaction", "trnsaction");
        rw.put("transaction_handle", "trnsaction_handle");
        rw.put("transfer", "trnsfer");
        rw.put("trigger", "triggr");
        rw.put("tring", "trng");
        rw.put("true", "tre");
        rw.put("trunc", "trnc");
        rw.put("truncate", "trncate");
        rw.put("tsequal", "tsqual");
        rw.put("type", "typ");
        rw.put("uid", "uidx");
        rw.put("unbuffered", "unbffered");
        rw.put("union", "unn");
        rw.put("unique", "unque");
        rw.put("unload", "unlad");
        rw.put("unloaddata", "unladdata");
        rw.put("unloadtable", "unladtable");
        rw.put("unlock", "unlck");
        rw.put("until", "untl");
        rw.put("up", "u");
        rw.put("update", "updte");
        rw.put("upper", "uppr");
        rw.put("usage", "usge");
        rw.put("use", "us");
        rw.put("used_pgs", "usd_pgs");
        rw.put("user", "usr");
        rw.put("using", "usng");
        rw.put("validate", "validte");
        rw.put("validrow", "vlidrow");
        rw.put("value", "val");
        rw.put("values", "vals");
        rw.put("varc", "vrc");
        rw.put("varch", "vrch");
        rw.put("varcha", "vrcha");
        rw.put("varchar", "varchr");
        rw.put("varchar2", "varch2r");
        rw.put("vargraphic", "vrgraphic");
        rw.put("verb_time", "vrb_time");
        rw.put("verify", "vrify");
        rw.put("version", "vrsion");
        rw.put("view", "vw");
        rw.put("wait", "wit");
        rw.put("waitfor", "witfor");
        rw.put("waiting", "witing");
        rw.put("warning", "wrning");
        rw.put("weekday", "wekday");
        rw.put("when", "whn");
        rw.put("whenever", "whenevr");
        rw.put("where", "whre");
        rw.put("while", "whle");
        rw.put("window", "wndow");
        rw.put("with", "wth");
        rw.put("without", "wthout");
        rw.put("work", "wrk");
        rw.put("wrap", "wrp");
        rw.put("write", "wrte");
        rw.put("writepass", "wrtepass");
        rw.put("writetext", "wrtetext");
        rw.put("year", "yr");
    }

    public int getMaxTableNameLength() {
        return maxTableNameLength;
    }

    /**
     * Set the max length in characters for a table name.
     */
    public void setMaxTableNameLength(int maxTableNameLength) {
        this.maxTableNameLength = maxTableNameLength;
    }

    public int getMaxColumnNameLength() {
        return maxColumnNameLength;
    }

    /**
     * Set the max length in characters for a column name.
     */
    public void setMaxColumnNameLength(int maxColumnNameLength) {
        this.maxColumnNameLength = maxColumnNameLength;
    }

    public String getPkSuffix() {
        return pkSuffix;
    }

    /**
     * Set the max length in characters for a constraint name.
     */
    public void setMaxConstraintNameLength(int maxConstraintNameLength) {
        this.maxConstraintNameLength = maxConstraintNameLength;
    }

    public int getMaxConstraintNameLength() {
        return maxConstraintNameLength;
    }

    /**
     * Set the max length in characters for an index name.
     */
    public void setMaxIndexNameLength(int maxIndexNameLength) {
        this.maxIndexNameLength = maxIndexNameLength;
    }

    public int getMaxIndexNameLength() {
        return maxIndexNameLength;
    }

    /**
     * Set the suffix added to table or field names to name primary key
     * columns. The default is _id so the pk for the employee table will
     * be employee_id.
     */
    public void setPkSuffix(String pkSuffix) {
        this.pkSuffix = pkSuffix;
    }

    public String getPkConstraintPrefix() {
        return pkConstraintPrefix;
    }

    /**
     * Set the prefix added to a table name to generate its primary key
     * constraint name.
     */
    public void setPkConstraintPrefix(String pkConstraintPrefix) {
        this.pkConstraintPrefix = pkConstraintPrefix;
    }

    /**
     * Set the string used to break 'words' when generating names from
     * field and class names and so on.
     */
    public void setWordBreak(String wordBreak) {
        this.wordBreak = wordBreak;
    }

    public String getWordBreak() {
        return wordBreak;
    }

    /**
     * Set the name used for sequence columns in link tables. These are used
     * to preserve the order of elements in ordered collections (lists and
     * arrays).
     */
    public void setSequenceColumnName(String sequenceColumnName) {
        this.sequenceColumnName = sequenceColumnName;
    }

    public String getSequenceColumnName() {
        return sequenceColumnName;
    }

    /**
     * Set the name used for value columns in link tables where the values
     * are not references to PC instances.
     */
    public void setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    public String getValueColumnName() {
        return valueColumnName;
    }

    /**
     * Set the name used for keys columns in link tables for maps where the
     * keys are not references to PC instances.
     */
    public void setKeyColumnName(String keyColumnName) {
        this.keyColumnName = keyColumnName;
    }

    public String getKeyColumnName() {
        return keyColumnName;
    }

    /**
     * Set the name used for classId columns. These are added to the table for
     * the base class in an inheritance hierarchy.
     */
    public void setClassIdColumnName(String classIdColumnName) {
        this.classIdColumnName = classIdColumnName;
    }

    public String getClassIdColumnName() {
        return classIdColumnName;
    }

    public String getIndexNamePrefix() {
        return indexNamePrefix;
    }

    /**
     * Set the prefix used to generate index names.
     */
    public void setIndexNamePrefix(String indexNamePrefix) {
        this.indexNamePrefix = indexNamePrefix;
    }

    /**
     * Generate a JDBC name from a java class or field name. This breaks
     * the name at each capital inserting underscores and converts to
     * lower case. If the name turns out to be a reserved word it is replaced.
     * Any leading underscores are removed.
     * '$' are also removed.
     */
    protected String getJdbcName(String name) {
        int n = name.length();
        StringBuffer ans = new StringBuffer(n + 4);
        int i = 0;
        for (; i < n && name.charAt(i) == '_'; i++) ;
        for (; i < n; i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                c = Character.toLowerCase(c);
                if (i > 0) ans.append(wordBreak);
            }
            if (c == '/') {
                ans.append(wordBreak);
            } else if (c != '$' && c != '.') {
                ans.append(c);
            }
        }
        name = ans.toString();
        String rep = (String)reservedWords.get(name);
        if (rep != null) name = rep;
        return name;
    }

    /**
     * Shrink the supplied name to maxlen chars if it is longer than maxlen.
     * This implementation removes vowels first and then truncates if it has
     * to.
     */
    protected String shrinkName(String name, int maxlen) {
        if (!nameCanStartWithUnderscore){
            int n = name.length();
            int j = 0;
            for (; j < n && name.charAt(j) == '_'; j++);
            name = name.substring(j,n);
        }
        int len = name.length();
        if (len <= maxlen) return name;
        int todo = len - maxlen;
        StringBuffer s = new StringBuffer();
        s.append(name.charAt(0));
        int i;
        for (i = 1; todo > 0 && i < len;) {
            char c = name.charAt(i++);
            if (c == 'e' || c == 'a' || c == 'i' || c == 'o' || c == 'u') {
                --todo;
            } else {
                s.append(c);
            }
        }
        if (todo == 0) {
            s.append(name.substring(i));
        }
        if (s.length() > maxlen) s.setLength(maxlen);
        return s.toString();
    }

    /**
     * Generate a new name comprised of name with i appended. Characters are
     * stripped from the end of name to make space for i if maxLen would
     * be exceeded. This is used to resolve generated name clashes.
     */
    protected String appendInt(String name, int i, int maxLen) {
        String is = Integer.toString(i);
        int cut = name.length() + is.length() - maxLen;
        if (cut > 0) {
            int len = maxLen - cut;
            if (len < 1) len = 1;
            return name.substring(0, len) + is;
        } else {
            return name + is;
        }
    }

    /**
     * Add a table name specified in jdo meta data.
     *
     * @throws IllegalArgumentException if the name is invalid
     *                                  (e.g. 'duplicate table name' or 'invalid character XXX in name'
     *                                  etc.)
     */
    public void addTableName(String name) throws IllegalArgumentException {
        if (tableMap.containsKey(name)) {
            throw BindingSupportImpl.getInstance().illegalArgument("Duplicate table name '" +
                    name + "'");
        }
        tableMap.put(name, new HashSet());
    }

    /**
     * Remove all information about table.
     */
    public void removeTableName(String name) {
        tableMap.remove(name);
    }

    /**
     * Generate a table name for a persistent class. The name generator must
     * 'add' it.
     *
     * @see #addTableName
     */
    public String generateClassTableName(String className) {
        int dot = className.lastIndexOf('.');
        if (dot >= 0) className = className.substring(dot + 1);



        String name = shrinkName(getJdbcName(className), maxTableNameLength);
        String n = name;
        for (int i = 2; tableMap.containsKey(n); i++) {
            n = appendInt(name, i, maxTableNameLength);
        }
        addTableName(n);
        return n;
    }

    /**
     * Generate a table name for a link table (normally used to hold the values
     * of a collection or array). The name generator must 'add' it.
     *
     * @param tableName        The table on the 1 side of the the link
     * @param fieldName        The field the link table is for
     * @param elementTableName The table on the n side of the link or null if
     *                         none (e.g. a link table for a collection of String's)
     * @see #addTableName
     */
    public String generateLinkTableName(String tableName, String fieldName,
            String elementTableName) {
        String name;
        if (elementTableName == null) {
            name = tableName + wordBreak + getJdbcName(fieldName);
        } else {
            name = tableName + wordBreak + elementTableName;
        }
        name = shrinkName(name, maxTableNameLength);
        String n = name;
        for (int i = 2; tableMap.containsKey(n); i++) {
            n = appendInt(name, i, maxTableNameLength);
        }
        addTableName(n);
        return n;
    }

    /**
     * Add the primary key constaint name specified in jdo meta data.
     *
     * @throws IllegalArgumentException if it is invalid
     */
    public void addPkConstraintName(String tableName, String pkConstraintName)
            throws IllegalArgumentException {
        if (constraintNames.contains(pkConstraintName)) {
            throw BindingSupportImpl.getInstance().illegalArgument("Duplicate constraint name: " +
                    pkConstraintName);
        }
        constraintNames.add(pkConstraintName);
    }

    /**
     * Generate a name for the primary key constaint for tableName.
     */
    public String generatePkConstraintName(String tableName) {
        int maxlen = maxConstraintNameLength - pkConstraintPrefix.length();
        String name = pkConstraintPrefix + shrinkName(tableName, maxlen);
        String n = name;
        for (int i = 2; constraintNames.contains(n); i++) {
            n = appendInt(name, i, maxConstraintNameLength);
        }
        addPkConstraintName(tableName, n);
        return n;
    }

    /**
     * Add the referential integrity constaint name specified in jdo meta data.
     *
     * @throws IllegalArgumentException if it is invalid
     */
    public void addRefConstraintName(String tableName,
            String refConstraintName)
            throws IllegalArgumentException {
        if (constraintNames.contains(refConstraintName)) {
            throw BindingSupportImpl.getInstance().illegalArgument("Duplicate constraint name: " +
                    refConstraintName);
        }
        constraintNames.add(refConstraintName);
    }

    /**
     * Generate a name for a referential integrity constaint for tableName.
     * The name generator must add it.
     *
     * @param tableName    The table with the constraint
     * @param refTableName The table being referenced
     * @param fkNames      The names of the foreign keys in tableName
     * @param refPkNames   The names of the primary key of refTableName
     * @see #addRefConstraintName
     */
    public String generateRefConstraintName(String tableName,
            String refTableName, String[] fkNames, String[] refPkNames) {
        int maxlen = maxConstraintNameLength - refConstraintPrefix.length();
        String name = refConstraintPrefix + shrinkName(
                tableName + wordBreak + refTableName, maxlen);
        String n = name;
        for (int i = 2; constraintNames.contains(n); i++) {
            n = appendInt(name, i, maxConstraintNameLength);
        }
        addRefConstraintName(tableName, n);
        return n;
    }

    /**
     * Add a column name. The tableName will have already been added.
     *
     * @throws IllegalArgumentException if the name is invalid
     *                                  (e.g. 'duplicate column name' or 'invalid character XXX in name'
     *                                  etc.)
     */
    public void addColumnName(String tableName, String columnName)
            throws IllegalArgumentException {
        String err = addColumnNameImp(tableName, columnName);
        if (err != null) throw BindingSupportImpl.getInstance().illegalArgument(err);
    }

    /**
     * Does the table contain the column?
     */
    public boolean isColumnInTable(String tableName, String columnName) {
        HashSet cols = (HashSet)tableMap.get(tableName);
        if (cols == null) {
            throw BindingSupportImpl.getInstance().internal(
                    "isColumnInTable called with unknown table: '" + tableName + "'");
        }
        return cols.contains(columnName);
    }

    /**
     * Add a column name. The tableName will have already been added.
     *
     * @return null if ok or error message
     */
    protected String addColumnNameImp(String tableName, String columnName) {
        HashSet cols = (HashSet)tableMap.get(tableName);
        if (cols == null) {
            throw BindingSupportImpl.getInstance().internal(
                    "addColumnName called with unknown table: '" + tableName + "'");
        }
        if (cols.contains(columnName)) {
            return "Duplicate column name '" + columnName +
                    "' in table '" + tableName + "'";
        }
        cols.add(columnName);
        return null;
    }

    /**
     * Generate and add a name for the primary key column for a PC class using
     * datastore identity.
     */
    public String generateDatastorePKName(String tableName) {
        String name = getPKNameForTableName(tableName, 0);
        for (int j = 2; addColumnNameImp(tableName, name) != null; j++) {
            name = getPKNameForTableName(tableName, j);
        }
        return name;
    }

    /**
     * Generate a primary key column name from a table name and an index.
     * If the index is less then 2 it must be ignored. Otherwise it must
     * be used in the generated name. This is used to resolve name clashes.
     * The returned name must not exceed maxColumnNameLength.
     */
    protected String getPKNameForTableName(String tableName, int i) {
        int maxlen = maxColumnNameLength - pkSuffix.length();
        if (i < 2) {
            return shrinkName(tableName, maxlen) + pkSuffix;
        } else {
            return appendInt(tableName, i, maxlen) + pkSuffix;
        }
    }

    /**
     * Generate and add the name for a classId column.
     *
     * @see #addColumnNameImp
     */
    public String generateClassIdColumnName(String tableName) {
        String name = shrinkName(classIdColumnName, maxColumnNameLength);
        for (int j = 2; addColumnNameImp(tableName, name) != null; j++) {
            name = appendInt(classIdColumnName, j, maxColumnNameLength);
        }
        return name;
    }

    /**
     * Generate and add the name for a field column.
     */
    public String generateFieldColumnName(String tableName, String fieldName,
            boolean primaryKey) {
        fieldName = fieldName.substring(fieldName.lastIndexOf('.')+1);
        String name = getColumnNameForFieldName(fieldName, 0);
        for (int j = 2; addColumnNameImp(tableName, name) != null; j++) {
            name = getColumnNameForFieldName(fieldName, j);
        }
        return name;
    }

    /**
     * Generate a column name from a field name and an index. If the index
     * is less then 2 it must be ignored. Otherwise it must be used in the
     * generated name. This is used to resolve name clashes. The returned
     * name must not exceed maxColumnNameLength.
     */
    protected String getColumnNameForFieldName(String fieldName, int i) {
        fieldName = getJdbcName(fieldName);
        if (i < 2) {
            return shrinkName(fieldName, maxColumnNameLength);
        } else {
            return appendInt(fieldName, i, maxColumnNameLength);
        }
    }

    /**
     * Generate a column name from a field name, a column number and an index.
     * If the index is less then 2 it must be ignored. Otherwise it must be
     * used in the generated name. This is used to resolve name clashes. The
     * columnNumber must also be used in the name. The returned name must not
     * exceed maxColumnNameLength. This is used for fields comprising of
     * multiple columns.
     */
    protected String getColumnNameForFieldName(String fieldName, int col,
            int i) {
        fieldName = getJdbcName(fieldName);
        String suffix = Integer.toString(col);
        int maxlen = maxColumnNameLength - suffix.length();
        if (i < 2) {
            return appendInt(fieldName, col, maxlen) + suffix;
        } else {
            return shrinkName(fieldName, maxlen) + suffix;
        }
    }

    /**
     * Generate and add names for one or more columns for a field that is
     * a reference to another PC class. Some of the columns may already have
     * names.
     *
     * @param columnNames  Store the column names here (some may already have
     *                     names if specified in the .jdo meta data)
     * @param refTableName The table being referenced (null if not a JDBC class)
     * @param refPkNames   The names of the primary key columns of refTableName
     * @param otherRefs    Are there other field referencing the same class here?
     * @throws IllegalArgumentException if any existing names are invalid
     */
    public void generateRefFieldColumnNames(String tableName,
            String fieldName, String[] columnNames, String refTableName,
            String[] refPkNames, boolean otherRefs) {
        if (refTableName == null) {
            columnNames[0] = generateFieldColumnName(tableName, fieldName,
                    false);
            return;
        }
        String prefix;
        if (otherRefs || tableName.equals(refTableName)) {
            int maxlen = 0;
            for (int i = refPkNames.length - 1; i >= 0; i--) {
                int n = refPkNames[i].length();
                if (n > maxlen) maxlen = n;
            }
            int n2 = maxColumnNameLength / 2 - 1;
            maxlen = n2 - maxlen;
            if (maxlen < n2) maxlen = n2;
            prefix = shrinkName(getJdbcName(fieldName), maxlen) + "_";
        } else {
            prefix = "";
        }
        int n = columnNames.length;
        for (int i = 0; i < n; i++) {
            String name = columnNames[i];
            if (name != null) {
                if (!isColumnInTable(tableName, name)) {
                    addColumnNameImp(tableName, name);
                }
                continue;
            }
            String base = prefix + refPkNames[i];
            name = getColumnNameForRefColumn(base, 0);
            for (int j = 2; addColumnNameImp(tableName, name) != null; j++) {
                name = getColumnNameForRefColumn(base, j);
            }
            columnNames[i] = name;
        }
    }

    /**
     * Generate a column name from a ref column name and an index. If the
     * index is less then 2 it must be ignored. Otherwise it must be used in
     * the generated name. This is used to resolve name clashes. The returned
     * name must not exceed maxColumnNameLength.
     */
    protected String getColumnNameForRefColumn(String base, int i) {
        if (i < 2) {
            return shrinkName(base, maxColumnNameLength);
        } else {
            return appendInt(base, i, maxColumnNameLength);
        }
    }

    /**
     * Generate and add names for one or more columns for a field that is
     * a polymorphic reference to any other PC class. Some of the columns may
     * already have names.
     *
     * @param columnNames Store the column names here (some may already have
     *                    names if specified in the .jdo meta data). The class-id column
     *                    is at index 0.
     * @throws IllegalArgumentException if any existing names are invalid
     */
    public void generatePolyRefFieldColumnNames(String tableName,
            String fieldName, String[] columnNames)
            throws IllegalArgumentException {
        String fieldJdbc = getJdbcName(fieldName);
        if (columnNames[0] == null) {
            String name = fieldJdbc + polyRefClassIdSuffix;
            String n = name;
            for (int j = 2; addColumnNameImp(tableName, n) != null; j++) {
                n = appendInt(name, j, maxColumnNameLength);
            }
            columnNames[0] = n;
        }
        int nc = columnNames.length;
        boolean onePk = nc == 2;
        for (int i = 1; i < nc; i++) {
            String name;
            if (onePk) {
                name = fieldJdbc + pkSuffix;
            } else {
                name = fieldJdbc + pkSuffix + (char)('a' + i - 1);
            }
            String n = name;
            for (int j = 2; addColumnNameImp(tableName, n) != null; j++) {
                n = appendInt(name, j, maxColumnNameLength);
            }
            columnNames[i] = n;
        }
    }

    /**
     * Generate and add names for the column(s) in a link table that reference
     * the primary key of the main table. Some of the columns may already
     * have names which must be kept (no need to add them).
     *
     * @param tableName        The link table
     * @param mainTablePkNames The names of the main table primary key
     * @param linkMainRefNames The corresponding column names in the link table
     */
    public void generateLinkTableMainRefNames(String tableName,
            String[] mainTablePkNames, String[] linkMainRefNames) {
        int len = linkMainRefNames.length;
        for (int i = 0; i < len; i++) {
            String name = linkMainRefNames[i];
            if (name != null) continue;
            name = mainTablePkNames[i];
            String n = name;
            for (int j = 2; addColumnNameImp(tableName, n) != null; j++) {
                n = appendInt(name, j, maxColumnNameLength);
            }
            linkMainRefNames[i] = n;
        }
    }

    /**
     * Generate and add the name for a the column in a link table that stores
     * the element sequence number.
     */
    public String generateLinkTableSequenceName(String tableName) {
        String n = sequenceColumnName;
        for (int j = 2; addColumnNameImp(tableName, n) != null; j++) {
            n = appendInt(sequenceColumnName, j, maxColumnNameLength);
        }
        return n;
    }

    /**
     * Generate and add names for the column(s) in a link table that reference
     * the primary key of the value table. This is called for collections of
     * PC classes. Some of the columns may already have names which must be
     * kept (no need to add them).
     *
     * @param tableName         The link table
     * @param valuePkNames      The names of the value table primary key (may be
     *                          null if the value class is not stored in JDBC)
     * @param valueClassName    The name of the value class
     * @param linkValueRefNames The corresponding column names in the link table
     */
    public void generateLinkTableValueRefNames(String tableName,
            String[] valuePkNames, String valueClassName,
            String[] linkValueRefNames, boolean key) {
        String valueBaseName = null;
        if (valuePkNames == null) {
            int dot = valueClassName.lastIndexOf('.');
            if (dot >= 0) valueClassName = valueClassName.substring(dot + 1);
            valueBaseName = shrinkName(getJdbcName(valueClassName),
                    maxColumnNameLength);
        }
        int len = linkValueRefNames.length;
        for (int i = 0; i < len; i++) {
            String name = linkValueRefNames[i];
            if (name != null) continue;
            if (valuePkNames == null) {
                name = valueBaseName;
            } else {
                name = valuePkNames[i];
            }
            String n = name;
            for (int j = 2; addColumnNameImp(tableName, n) != null; j++) {
                n = appendInt(name, j, maxColumnNameLength);
            }
            linkValueRefNames[i] = n;
        }
    }

    /**
     * Generate and add the name for a the column in a link table that stores
     * the value where the value is not a PC class (int, String etc).
     *
     * @param tableName The link table
     * @param valueCls  The value class
     * @param key       Is this a key in a link table for a map?
     */
    public String generateLinkTableValueName(String tableName,
            Class valueCls, boolean key) {
        String name = key ? keyColumnName : valueColumnName;
        String n = name;
        for (int j = 2; addColumnNameImp(tableName, n) != null; j++) {
            n = appendInt(name, j, maxColumnNameLength);
        }
        return n;
    }

    /**
     * Add an index name. The tableName will have already been added.
     */
    public void addIndexName(String tableName, String indexName) {
        // we do not throw a exception if there are duplicates, because some DB's
        // do it on DB level and some on Table level.
        // If openaccess generates a Index name then we always do it on DB level.
        indexNames.add(indexName);
    }

    /**
     * Generate and add an index name.
     *
     * @see #addIndexName
     */
    public String generateIndexName(String tableName, String[] columnNames) {
        StringBuffer s = new StringBuffer();
        s.append(indexNamePrefix);
        s.append(tableName);
        s.append(wordBreak);
        int nc = columnNames.length;
        for (int i = 0; i < nc; i++) {
            if (i > 0) s.append(wordBreak);
            s.append(columnNames[i]);
        }
        String name = shrinkName(s.toString(), maxIndexNameLength);
        String n = name;
        for (int i = 2; indexNames.contains(n); i++) {
            n = appendInt(name, i, maxIndexNameLength);
        }
        addIndexName(tableName, n);
        return n;
    }

    public String getPolyRefClassIdSuffix() {
        return polyRefClassIdSuffix;
    }

    public void setPolyRefClassIdSuffix(String polyRefClassIdSuffix) {
        this.polyRefClassIdSuffix = polyRefClassIdSuffix;
    }
}

