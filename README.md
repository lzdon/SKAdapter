## SKRecyclerViewAdapter
Make RecyclerView.Adapter simpler

Support MutilType Layout , Kotlin, Functional Programming

### Dependency

Include the library in your `build.gradle`

```
dependencies{
    implementation 'com.lzd:skadapter:1.0.0'
}
```

## Usage
1. 定义数据类
```kotlin
class Item1(val title: String, val subTitle: String)

class Item2(val title: String, val subTitle: String)
```

2. 初始化`adpter`
```java
private fun initView(view: View) {
	val adapter = SKRecyclerViewAdapter<Any>()

	adapter.register(ofSKHolderFactory<Item1>(layoutRes = R.layout.item1){ 
	    // bind view here
	    itemView.text1 = it.title
	    itemView.text2 = it.subTitle
	})
	adapter.register(ofSKHolderFactory<Item2>(layoutRes = R.layout.item2){
	    // bind view here
	    itemView.text1 = it.title
	    itemView.text2 = it.subTitle
	})
}
```

3. 注入数据
```java
val result = arrayListOf<Any>()

result.add(Item1("title", "subTitle"))
result.add(Item2("title", "subTitle"))

adapter.setItems(result)
```

4. `kotlin`只需上面三步，如果使用`java`，需要实现`SKViewHolder`和`SKViewHolderFactory`
```java
private void initView(View view) {
    ActionCallback callback = (item, itemView) -> { };
    SKRecyclerViewAdapter adapter = new SKRecyclerViewAdapter();
    adapter.register(new ItemOneViewHolder.Factory(),new ItemTwoViewHolder.Factory(callback));
}

static class ItemOneViewHolder extends SKViewHolder<Item1> {
    
    static class Factory extends SKViewHolderFactory<Item1>{

        @NotNull
        @Override
        public SKViewHolder<Item1> createViewHolder(@NotNull ViewGroup viewGroup) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item1, viewGroup, false);
            return new ItemOneViewHolder(itemView);
        }
    }

    public ItemOneViewHolder(@NotNull View view) {
        super(view);
    }

    @Override
    public void onBind(@NotNull Item1 item) {
        // bind view here
    }
}

static class ItemTwoViewHolder extends SKViewHolder<Item2> {

    static class Factory extends SKViewHolderFactory<Item2>{

    	private ActionCallback actionCallback;

    	// use constructor injection if you need a callback
    	public Factory(ActionCallback callback) {
    		this.actionCallback = callback;
	    }

        @NotNull
        @Override
        public SKViewHolder<Item2> createViewHolder(@NotNull ViewGroup viewGroup) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item2, viewGroup, false);
            return new ItemTwoViewHolder(itemView);
        }
    }

    public ItemTwoViewHolder(@NotNull View view, ActionCallback callback) {
        super(view);
        itemView.setOnClickListener{v -> callback.onItemClick(item, itemView)}
    }

    @Override
    public void onBind(@NotNull Item2 item) {
        // bind view here
    }
}

interface ActionCallback{
	void onItemClick(Item1 item, View itemView);
}
```

## More
- SKPlaceHolderAdapter - 支持空提示、错误占位图的Adapter
使用：
```java
val adapter = SKPlaceHolderAdapter()
adapter.showErrorView{ 
	// invoke when view clicked
}
adapter.showEmptyView{ 
	// invoke when view clicked
}
```
定制自己的空提示、错误布局：
```java
// global
sDefaultErrorFactory= ofSKHolderFactory(layoutRes = R.layout.custom_error) { 
    // bind view here
}
sDefaultEmptyFactory= ofSKHolderFactory(layoutRes = R.layout.custom_empty) {
    // bind view here
}
```
```java
// constructor injection
val emptyViewFactory = ofSKHolderFactory<EmptyViewData>(layoutRes = R.layout.custom_empty) {
    // bind view here
}
val errorViewFactory = ofSKHolderFactory<ErrorViewData>(layoutRes = R.layout.custom_error) {
    // bind view here
}
val adapter = SKPlaceHolderAdapter(emptyViewFactory, errorViewFactory)
```

- SKAutoPageAdapter - 支持自动分页加载的Adapter
```java
val adapter = SKAutoPageAdapter()
adapter.onLoadNextPage = {
	// invoke when load next page 
}
// first page
adapter.setPageItems(dataList, hasMore)
// append page
adapter.appendPageItems(dataList, hasMore)
// load failed
adapter.onLoadPageDataFailed()
```