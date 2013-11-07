package org.wikapidia.core.cookbook;

import org.wikapidia.conf.Configuration;
import org.wikapidia.conf.ConfigurationException;
import org.wikapidia.conf.Configurator;
import org.wikapidia.core.cmd.Env;
import org.wikapidia.core.cmd.EnvBuilder;
import org.wikapidia.core.dao.*;
import org.wikapidia.core.lang.LanguageSet;
import org.wikapidia.core.lang.LocalId;
import org.wikapidia.core.model.*;

/**
 */
public class UniversalPageDaoExample {
    public static void main(String args[]) throws ConfigurationException, DaoException {

        Env env = new EnvBuilder().build();
        Configurator configurator = env.getConfigurator();
        UniversalPageDao upDao = configurator.get(UniversalPageDao.class);
        LocalPageDao lpDao = configurator.get(LocalPageDao.class);

        // get the languages that have been loaded in the current configuration
        MetaInfoDao miDao = configurator.get(MetaInfoDao.class);
        LanguageSet ls = miDao.getLoadedLanguages();

        // will print only "global" UniversalPages in the article namespace, or UniversalPages that have LocalPages in all languages in ls and that are in the article namespace
        printAllUPsWithPagesInAtleastNLanguages(ls.size(),NameSpace.ARTICLE,ls,upDao,lpDao);

        // will print all UniversalPages in the category namespace
//        printAllUPsWithPagesInAtleastNLanguages(1,NameSpace.CATEGORY,ls,upDao,lpDao);



    }

    /**
     * Print the titles of all universal pages of all universal pages that have articles in at least n languages.
     * @param n Minimum number of languages
     * @param ns The namespace to consider, e.g. NameSpace.ARTICLE. Note: if null, will consider pages in all namespaces.
     * @param upDao
     * @param lpDao
     */
    public static void printAllUPsWithPagesInAtleastNLanguages(int n, NameSpace ns, LanguageSet ls, UniversalPageDao upDao, LocalPageDao lpDao) throws DaoException {

        Iterable<UniversalPage> pages;
        if (ns != null){
            pages = (Iterable<UniversalPage>)upDao.get(new DaoFilter().setNameSpaces(ns).setLanguages(ls));
        }else{
            pages = (Iterable<UniversalPage>)upDao.get(new DaoFilter().setLanguages(ls));
        }

        int matchCount = 0;
        int totalPageCount = 0;
        for (UniversalPage page : pages){
            if (page.getNumberOfLanguages() >= n){
                System.out.print(page.getUnivId() + ": ");
                for (LocalId lId : page.getLocalEntities()){
                    LocalPage lPage = lpDao.getById(lId.getLanguage(), lId.getId());
                    System.out.print(lPage.getTitleAndLanguageAsString() + "\t");
                }
                matchCount++;
                System.out.println();
            }

        }
        System.out.printf("Found %d UniversalPages with articles >=%d languages out of %d total pages found.\n", matchCount, n, totalPageCount);
        if (ns != null) System.out.println("Only considered namespace = " + ns);


    }
}
