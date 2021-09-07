package iCalendarEditor;

import java.util.ArrayList;

//Enable the object to be put in the quick-using ArrayList<Object>
//Not used yet
public interface QuickOption extends Cloneable {

	ArrayList<Object> quickOption = new ArrayList<>();

	// Add method for QuickOption
	public static void add(Object obj) {
		// Check whether a identical object exist in the quickOption
		boolean flag = true;
		for (int i = 0; i < quickOption.size(); i++) {
			if (obj.equals(quickOption.get(i))) {
				flag = false;
				break;
			}
			// Special case: obj is a instance of String[] (location)
			else if (obj instanceof String[] && quickOption.get(i) instanceof String[]) {
				if (((String[]) obj).length == ((String[]) quickOption.get(i)).length) {
					flag = false;
					for (int j = 0; j < ((String[]) obj).length; j++) {
						if (!((String[]) obj)[j].equals(((String[]) quickOption.get(i))[j])) {
							flag = true;
							break;
						}
					}
					if (!flag)
						break;
				}
			}
		}
		// obj is a new object -> add
		if (flag) {
			quickOption.add(obj);
			// Size control
			if (quickOption.size() > 20)
				quickOption.remove(0);
		}
	}

	// Add quick usable objects to the options
	public default void storeAsQuickOption() {
		QuickOption.add(this.clone());
	}

	Object clone();
}
