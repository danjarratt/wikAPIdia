package org.wikapidia.core.model;

import com.google.common.collect.Multimap;
import org.wikapidia.core.lang.Language;
import org.wikapidia.core.lang.LanguageSet;
import org.wikapidia.core.lang.LocalId;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Brent Hecht
 */
public class UniversalPage extends AbstractUniversalEntity<LocalId> {

    /**
     * The universal id for the universal page. Universal ids are defined within but not across namespaces.
     */
    private final int univId;
    private final NameSpace nameSpace;

    public UniversalPage(int univId, int algorithmId) {
        super(algorithmId);
        this.univId = univId;
        this.nameSpace = null;
    }

    public UniversalPage(int univId, int algorithmId, NameSpace nameSpace, Multimap<Language, LocalId> localPages) {
        super(algorithmId, localPages);
        this.univId = univId;
        this.nameSpace = nameSpace;
    }

    public UniversalPage(int univId, int algorithmId, NameSpace nameSpace, LanguageSet languages) {
        super(algorithmId, languages);
        this.univId = univId;
        this.nameSpace = nameSpace;
    }

    public int getUnivId(){
        return univId;
    }

    public NameSpace getNameSpace() {
        return nameSpace;
    }

//    public Collection<LocalId> getLocalIds(Language language) {
//        return new ArrayList<LocalId>(getLocalEntities(language));
//    }

    public static interface LocalPageChooser {
        public LocalId choose(Collection<LocalId> localPages);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UniversalPage) {
            UniversalPage other = (UniversalPage) o;
            return (this.getUnivId() == other.getUnivId() &&
                    this.getAlgorithmId() == other.getAlgorithmId());
        } else {
            return false;
        }
    }
}
