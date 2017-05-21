import {Component, OnInit} from "@angular/core";
import {INglDatatableRowClick, INglDatatableSort} from "ng-lightning/ng-lightning";
import {ArticleService} from "./article.service";

@Component({
    selector: 'app-article',
    templateUrl: './article.component.html',
    styleUrls: ['./article.component.css'],
    providers: [ArticleService]
})
export class ArticleComponent implements OnInit {
    private data;

    private page:number;
    private total:number;

    sort: INglDatatableSort = {key: 'id', order: 'asc'};

    constructor(private articleService: ArticleService) {
    }

    ngOnInit(): void {
        this.onPageChange(1);
    }

    onSort($event: INglDatatableSort) {
        const {key, order} = $event;
        this.data.sort((a: any, b: any) => {
            return (key === 'id' ? b[key] - a[key] : b[key].localeCompare(a[key])) * (order === 'desc' ? 1 : -1);
        });
    }

    onRowClick($event: INglDatatableRowClick) {
    }

    onPageChange(pageNumber:number){
        this.articleService.getArticles(pageNumber,10).subscribe(articles => {
            this.data = articles.content;
            this.page = articles.number+1;
            this.total = articles.totalElements;
        })
    }
}
