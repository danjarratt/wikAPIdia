package org.wikapidia.core.model;

import org.wikapidia.core.lang.Language;
import org.wikapidia.core.lang.LocalId;

/**
 */
public class LocalPage {

    protected final Language language;
    protected final int localId;
    protected final Title title;
    protected final NameSpace nameSpace;
    protected final boolean isRedirect;
    protected final boolean isDisambig;

    /**
     * Default for NON-redirect pages.
     * @param language
     * @param localId
     * @param title
     * @param nameSpace
     */
    public LocalPage(Language language, int localId, Title title, NameSpace nameSpace){
        this.language = language;
        this.localId = localId;
        this.title = title;
        this.nameSpace = nameSpace;
        isRedirect = false;
        isDisambig = false;
    }

    /**
     * Ability to set redirect pages.
     * @param language
     * @param localId
     * @param title
     * @param nameSpace
     * @param redirect
     */
    public LocalPage(Language language, int localId, Title title, NameSpace nameSpace, boolean redirect, boolean disambig) {
        this.language = language;
        this.localId = localId;
        this.title = title;
        this.nameSpace = nameSpace;
        isRedirect = redirect;
        isDisambig = disambig;
    }

    /**
     * Returns the language edition-defined id of this page. This id was given to this page when it was created in its language edition.
     * @return
     */
    public int getLocalId() {
        return localId;
    }

    /**
     * Returns the title of this local page.
     * @return
     */
    public Title getTitle() {
        return title;
    }

    /**
     * Returns the language edition in which this local page exists.
     * @return
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Returns the namespace of this LocalPage. See http://en.wikipedia.org/wiki/Wikipedia%3ANamespace for more information.
     * Note that not all languages use the same namespaces.
     * @return
     */
    public NameSpace getNameSpace() {
        return nameSpace;
    }

    /**
     * Returns true if this is a disambiguation page (e.g. http://en.wikipedia.org/wiki/Ms). See http://en.wikipedia.org/wiki/Help:Disambiguation for more information.
     * @return
     */
    public boolean isDisambig() {
        return isDisambig;
    }

    /**
     * Returns true if this is a redirect page. See http://en.wikipedia.org/wiki/Wikipedia:Redirect for more information.
     * @return
     */
    public boolean isRedirect() {
        return isRedirect;
    }

    public int hashCode(){
        return (language.getId() + "_" + localId).hashCode(); //non-optimal
    }

    /**
     * Gets a LocalId instance with the information from this LocalPage. LocalIds are simple (language + local id) structs.
     * @return
     */
    public LocalId toLocalId() {
        return new LocalId(language, localId);
    }

    public boolean equals(Object o){
        if (o instanceof LocalPage){
            LocalPage input = (LocalPage)o;
            return (input.getLanguage().equals(this.getLanguage()) &&
                    input.getLocalId() == this.getLocalId()
            );
        } else {
            return false;
        }
    }

    /**
     * Returns a simple description of this page in the format 'Title (language code)', e.g. "United States (en)".
     * Mimics format used by Hecht and Gergle (2009 + 2010).
     * @return
     */
    public String getTitleAndLanguageAsString(){
        return String.format("%s (%s)", title.toString(), language.toString());
    }

    @Override
    public String toString() {
        return "LocalPage{" +
                "nameSpace=" + nameSpace +
                ", title=" + title +
                ", localId=" + localId +
                ", language=" + language +
                '}';
    }
}