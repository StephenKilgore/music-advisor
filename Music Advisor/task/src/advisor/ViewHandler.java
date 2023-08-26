package advisor;

import java.util.ArrayList;
import java.util.LinkedList;

public class ViewHandler<T>{
    private LinkedList<T> objList = new LinkedList<>();
    private int pageSize = 0;
    private int currentPage = 1;
    private int totalPages = 0;
    private String pageTitle;

    public ViewHandler(int pageSize)
    {

           this.pageSize = pageSize;
    }
    public LinkedList<T> getObjList() {
        return objList;
    }
    public void setObjList(LinkedList<T> objList) {
        this.objList = objList;
        this.totalPages = objList.size() / pageSize;
        if (objList.size() % pageSize != 0)
        {
            totalPages++;
        }
    }
    public int getPageSize() {
        return pageSize;
    }
    public int getCurrentPage() {
        return currentPage;
    }
    public int getTotalPages() {
        return totalPages;
    }
    public String getPageTitle() {
        return "---PAGE " + currentPage + " OF " + totalPages + "---";
    }
    public void next()
    {
        if (currentPage < totalPages)
        {
            currentPage++;
            display();
        }
        else {
            System.out.println("No more pages");
        }
    }
    public void prev()
    {
        if (currentPage-1 != 0)
        {
            currentPage--;
            display();
        }
        else {
            System.out.println("No more pages");
        }
    }
    public void display()
    {
        int startingIndex = (currentPage - 1) * pageSize;
        int endingIndex = Math.min(startingIndex + pageSize, objList.size());

        for (T obj : objList.subList(startingIndex, endingIndex))
        {
            System.out.println(obj);
        }
        System.out.println(getPageTitle());
    }
}
