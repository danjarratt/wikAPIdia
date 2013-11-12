package org.wikapidia.core.dao.live;

import com.google.gson.JsonObject;
import com.typesafe.config.Config;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.wikapidia.conf.Configuration;
import org.wikapidia.conf.ConfigurationException;
import org.wikapidia.conf.Configurator;
import org.wikapidia.core.dao.DaoException;
import org.wikapidia.core.dao.DaoFilter;
import org.wikapidia.core.dao.RedirectDao;
import org.wikapidia.core.lang.Language;
import org.wikapidia.core.lang.LanguageSet;
import org.wikapidia.core.model.LocalPage;
import org.wikapidia.core.model.Redirect;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: derian
 * Date: 11/9/13
 * Time: 4:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class RedirectLiveDao implements RedirectDao {

    public RedirectLiveDao() throws DaoException {}

    //Notice: A DaoException will be thrown if you call the methods below!
    public void clear()throws DaoException{
        throw new UnsupportedOperationException("Can't use this method for remote wiki server!");
    }
    public void beginLoad()throws DaoException{
        throw new UnsupportedOperationException("Can't use this method for remote wiki server!");
    }
    public void endLoad()throws DaoException{
        throw new UnsupportedOperationException("Can't use this method for remote wiki server!");
    }
    public void save(Redirect a)throws DaoException{
        throw new UnsupportedOperationException("Can't use this method for remote wiki server!");
    }
    public int getCount(DaoFilter a)throws DaoException{
        throw new UnsupportedOperationException("Can't use this method for remote wiki server!");
    }
    public Iterable<Redirect> get(DaoFilter a)throws DaoException{
        throw new UnsupportedOperationException("Can't use this method for remote wiki server!");
    }
    public LanguageSet getLoadedLanguages() throws DaoException {
        throw new UnsupportedOperationException("Can't use this method for remote wiki server!");
    }

    public void save(Language lang, int src, int dest) throws DaoException {
        throw new UnsupportedOperationException("Can't use this method for remote wiki server!");
    }

    public Integer resolveRedirect(Language lang, int id) throws DaoException {
        String queryArgs = "&prop=info&redirects&pageids=" + id;
        JsonObject queryReply = LiveUtils.parseQueryObject(LiveUtils.getQueryJson(lang, queryArgs));
        int redirectId = LiveUtils.getIntsFromJsonObject(LiveUtils.getJsonObjectFromQueryObject(queryReply, "pages"), "pageid").get(0);
        if (redirectId != id) {
            return redirectId;
        }
        return null;
    }

    public boolean isRedirect(Language lang, int id) throws DaoException {
        String queryArgs = "&prop=info&pageids=" + id;
        JsonObject queryReply = LiveUtils.parseQueryObject(LiveUtils.getQueryJson(lang, queryArgs));
        String redirect = LiveUtils.getStringsFromJsonObject(LiveUtils.getJsonObjectFromQueryObject(queryReply, "pages"), "pageid").get(0);
        if (redirect == null) {
            return false;
        }
        return true;
    }

    public TIntSet getRedirects(LocalPage localPage) throws DaoException {
        List<Integer> redirects = getRedirectsFromId(localPage.getLanguage(), localPage.getLocalId());
        return new TIntHashSet(redirects);
    }


    public List<Integer> getRedirectsFromId(Language lang, int localId) throws DaoException {
        String queryArgs = "&list=backlinks&blfilterredir=redirects&blpageid=" + localId;
        JsonObject queryReply = LiveUtils.parseQueryObject(LiveUtils.getQueryJson(lang, queryArgs));
        return LiveUtils.getIntsFromJsonArray(LiveUtils.getJsonArrayFromQueryObject(queryReply, "backlinks"), "pageid");
    }

    public TIntIntMap getAllRedirectIdsToDestIds(Language lang) throws DaoException {
        TIntIntMap redirects = new TIntIntHashMap();
        String queryArgs = "&list=allpages&apfrom=&apfilterredir=redirects";
        JsonObject queryReply = LiveUtils.parseQueryObject(LiveUtils.getQueryJson(lang, queryArgs));
        List<Integer> redirectIds = LiveUtils.getIntsFromJsonArray(LiveUtils.getJsonArrayFromQueryObject(queryReply, "allpages"), "pageid");

        for (int sourceId : redirectIds) {
            int destId = resolveRedirect(lang, sourceId);
            redirects.put(sourceId, destId);
        }

        return  redirects;
    }

    public static class Provider extends org.wikapidia.conf.Provider<RedirectDao> {
        public Provider(Configurator configurator, Configuration config) throws ConfigurationException {
            super(configurator, config);
        }

        @Override
        public Class getType() {
            return RedirectDao.class;
        }

        @Override
        public String getPath() {
            return "dao.redirect";
        }

        @Override
        public RedirectDao get(String name, Config config) throws ConfigurationException {
            if (!config.getString("type").equals("live")) {
                return null;
            }
            try {
                return new RedirectLiveDao();

            } catch (DaoException e) {
                throw new ConfigurationException(e);
            }
        }
    }

}