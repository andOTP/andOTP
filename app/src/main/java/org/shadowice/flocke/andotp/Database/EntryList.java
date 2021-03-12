package org.shadowice.flocke.andotp.Database;

import org.shadowice.flocke.andotp.Utilities.Constants;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class EntryList {
    private final ArrayList<Entry> entries;
    private final AtomicLong currentId = new AtomicLong();

    public EntryList() {
        entries = new ArrayList<>();
    }

    public boolean addEntry(Entry newEntry) {
        return addEntry(newEntry, false);
    }

    public boolean addEntry(Entry newEntry, boolean update) {
        if (! entries.contains(newEntry)) {
            long newId = currentId.incrementAndGet();
            newEntry.setListId(newId);

            entries.add(newEntry);

            return true;
        } else {
            if (update) {
                int oldIdx = entries.indexOf(newEntry);
                Entry oldEntry = entries.get(oldIdx);

                newEntry.setListId(oldEntry.getListId());
                entries.set(oldIdx, newEntry);
            }
        }

        return false;
    }

    public void updateEntries(ArrayList<Entry> newEntries, boolean update) {
        // Remove all items not in the new list
        entries.retainAll(newEntries);

        // Add new and update existing entries
        for (Entry e : newEntries) {
            addEntry(e, update);
        }
    }

    public Entry getEntry(int pos) {
        return entries.get(pos);
    }

    public void swapEntries(int fromPosition, int toPosition) {
        Collections.swap(entries, fromPosition, toPosition);
    }

    public void removeEntry(int pos) {
        entries.remove(pos);
    }

    public int indexOf(Entry e) {
        return entries.indexOf(e);
    }

    public boolean isEqual(ArrayList<Entry> otherEntries) {
        return entries.equals(otherEntries);
    }

    public ArrayList<Entry> getEntries() {
        return new ArrayList<>(entries);
    }

    public ArrayList<Entry> getEntriesSorted(Constants.SortMode sortMode) {
        return sortEntries(entries, sortMode);
    }

    public static ArrayList<Entry> sortEntries(ArrayList<Entry> unsortedEntries, Constants.SortMode sortMode) {
        ArrayList<Entry> sorted = new ArrayList<>(unsortedEntries);

        if (sortMode == Constants.SortMode.ISSUER) {
            Collections.sort(sorted, new IssuerComparator());
        } else if (sortMode == Constants.SortMode.LABEL) {
            Collections.sort(sorted, new LabelComparator());
        } else if (sortMode == Constants.SortMode.LAST_USED) {
            Collections.sort(sorted, new LastUsedComparator());
        } else if (sortMode == Constants.SortMode.MOST_USED) {
            Collections.sort(sorted, new MostUsedComparator());
        }

        return sorted;
    }

    public ArrayList<String> getAllTags() {
        HashSet<String> tags = new HashSet<>();

        for(Entry entry : entries) {
            tags.addAll(entry.getTags());
        }

        return new ArrayList<>(tags);
    }

    public ArrayList<Entry> getFilteredEntries(CharSequence constraint, List<Constants.SearchIncludes> filterValues, Constants.SortMode sortMode) {
        ArrayList<Entry> filtered = new ArrayList<>();

        if (constraint != null && constraint.length() != 0){
            for (int i = 0; i < entries.size(); i++) {
                if (filterValues.contains(Constants.SearchIncludes.LABEL) && entries.get(i).getLabel().toLowerCase().contains(constraint.toString().toLowerCase())) {
                    filtered.add(entries.get(i));
                } else if (filterValues.contains(Constants.SearchIncludes.ISSUER) && entries.get(i).getIssuer().toLowerCase().contains(constraint.toString().toLowerCase())) {
                    filtered.add(entries.get(i));
                } else if (filterValues.contains(Constants.SearchIncludes.TAGS)) {
                    List<String> tags = entries.get(i).getTags();
                    for (int j = 0; j < tags.size(); j++) {
                        if (tags.get(j).toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filtered.add(entries.get(i));
                            break;
                        }
                    }
                }
            }
        } else {
            filtered = entries;
        }

        return sortEntries(filtered, sortMode);
    }

    public ArrayList<Entry> getEntriesFilteredByTags(List<String> tags, boolean noTags, Constants.TagFunctionality tagFunctionality, Constants.SortMode sortMode) {
        ArrayList<Entry> matchingEntries = new ArrayList<>();

        for(Entry e : entries) {
            // Entries with no tags will always be shown
            boolean foundMatchingTag = e.getTags().isEmpty() && noTags;

            if(tagFunctionality == Constants.TagFunctionality.AND) {
                if(e.getTags().containsAll(tags)) {
                    foundMatchingTag = true;
                }
            } else {
                for (String tag : tags) {
                    if (e.getTags().contains(tag)) {
                        foundMatchingTag = true;
                        break;
                    }
                }
            }

            if(foundMatchingTag) {
                matchingEntries.add(e);
            }
        }

        return sortEntries(matchingEntries, sortMode);
    }

    public static class IssuerComparator implements Comparator<Entry> {
        Collator collator;

        IssuerComparator(){
            collator = Collator.getInstance();
            collator.setStrength(Collator.PRIMARY);
        }

        @Override
        public int compare(Entry o1, Entry o2) {
            return collator.compare(o1.getIssuer(), o2.getIssuer());
        }
    }

    public static class LabelComparator implements Comparator<Entry> {
        Collator collator;

        LabelComparator(){
            collator = Collator.getInstance();
            collator.setStrength(Collator.PRIMARY);
        }

        @Override
        public int compare(Entry o1, Entry o2) {
            return collator.compare(o1.getLabel(), o2.getLabel());
        }
    }

    public static class LastUsedComparator implements Comparator<Entry> {
        @Override
        public int compare(Entry o1, Entry o2) {
            return Long.compare(o2.getLastUsed(), o1.getLastUsed());
        }
    }

    public static class MostUsedComparator implements Comparator<Entry> {
        @Override
        public int compare(Entry o1, Entry o2) {
            return Long.compare(o2.getUsedFrequency(), o1.getUsedFrequency());
        }
    }
}
