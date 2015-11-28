package ru.ifmo.acm.utils;

import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractBeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ItemSorter;
import com.vaadin.data.util.filter.UnsupportedFilterException;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedBeanItemContainer<BEANTYPE> extends BeanItemContainer<BEANTYPE> {
    public SynchronizedBeanItemContainer(Class<? super BEANTYPE> type) throws IllegalArgumentException {
        super(type);
    }

    public SynchronizedBeanItemContainer(Class<? super BEANTYPE> type, Collection<? extends BEANTYPE> collection) throws IllegalArgumentException {
        super(type, collection);
    }

    @Override
    public void addAll(Collection<? extends BEANTYPE> collection) {
        containerLock.lock();
        try {
            super.addAll(collection);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public BeanItem<BEANTYPE> addItemAfter(Object previousItemId, Object newItemId) throws IllegalArgumentException {
        containerLock.lock();
        try {
            return super.addItemAfter(previousItemId, newItemId);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public BeanItem<BEANTYPE> addItemAt(int index, Object newItemId) throws IllegalArgumentException {
        containerLock.lock();
        try {
            return super.addItemAt(index, newItemId);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public BeanItem<BEANTYPE> addItem(Object itemId) {
        containerLock.lock();
        try {
            return super.addItem(itemId);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public BeanItem<BEANTYPE> addBean(BEANTYPE bean) {
        containerLock.lock();
        try {
            return this.addItem(bean);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public boolean removeAllItems() {
        containerLock.lock();
        try {
            return super.removeAllItems();
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public synchronized Class<? super BEANTYPE> getBeanType() {
        containerLock.lock();
        try {
            return super.getBeanType();
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public Collection<String> getContainerPropertyIds() {
        containerLock.lock();
        try {
            return super.getContainerPropertyIds();
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public BeanItem<BEANTYPE> getItem(Object itemId) {
        containerLock.lock();
        try {
            return this.getUnfilteredItem(itemId);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public List<BEANTYPE> getItemIds() {
        containerLock.lock();
        try {
            return super.getItemIds();
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        containerLock.lock();
        try {
            return super.getContainerProperty(itemId, propertyId);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public boolean removeItem(Object itemId) {
        containerLock.lock();
        try {
            return super.removeItem(itemId);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        containerLock.lock();
        try {
            super.valueChange(event);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public void addContainerFilter(Object propertyId, String filterString, boolean ignoreCase, boolean onlyMatchPrefix) {
        containerLock.lock();
        try {
            super.addContainerFilter(propertyId, filterString, ignoreCase, onlyMatchPrefix);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public void removeAllContainerFilters() {
        containerLock.lock();
        try {
            super.removeAllContainerFilters();
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public void removeContainerFilters(Object propertyId) {
        containerLock.lock();
        try {
            super.removeContainerFilters(propertyId);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public void addContainerFilter(Filter filter) throws UnsupportedFilterException {
        containerLock.lock();
        try {
            super.addContainerFilter(filter);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public void removeContainerFilter(Filter filter) {
        containerLock.lock();
        try {
            super.removeContainerFilter(filter);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public boolean hasContainerFilters() {
        containerLock.lock();
        try {
            return super.hasContainerFilters();
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public Collection<Filter> getContainerFilters() {
        containerLock.lock();
        try {
            return super.getContainerFilters();
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public Collection<?> getSortableContainerPropertyIds() {
        containerLock.lock();
        try {
            return super.getSortableContainerPropertyIds();
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        containerLock.lock();
        try {
            super.sort(propertyId, ascending);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public ItemSorter getItemSorter() {
        containerLock.lock();
        try {
            return super.getItemSorter();
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public void setItemSorter(ItemSorter itemSorter) {
        containerLock.lock();
        try {
            super.setItemSorter(itemSorter);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public AbstractBeanContainer.BeanIdResolver<BEANTYPE, BEANTYPE> getBeanIdResolver() {
        containerLock.lock();
        try {
            return super.getBeanIdResolver();
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public void addPropertySetChangeListener(PropertySetChangeListener listener) {
        containerLock.lock();
        try {
            super.addPropertySetChangeListener(listener);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public void removePropertySetChangeListener(PropertySetChangeListener listener) {
        containerLock.lock();
        try {
            super.removePropertySetChangeListener(listener);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {
        containerLock.lock();
        try {
            return super.addContainerProperty(propertyId, type, defaultValue);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public boolean addNestedContainerProperty(String propertyId) {
        containerLock.lock();
        try {
            return super.addNestedContainerProperty(propertyId);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public void addNestedContainerBean(String propertyId) {
        containerLock.lock();
        try {
            super.addNestedContainerBean(propertyId);
        } finally {
            containerLock.unlock();
        }
    }

    @Override
    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        containerLock.lock();
        try {
            return super.removeContainerProperty(propertyId);
        } finally {
            containerLock.unlock();
        }
    }



    private final Lock containerLock = new ReentrantLock();
}
