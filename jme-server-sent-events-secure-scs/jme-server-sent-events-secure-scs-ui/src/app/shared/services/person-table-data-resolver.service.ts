import {Injectable} from '@angular/core';
import {map, Observable} from 'rxjs';
import {PersonDto} from '../models/personDto';
import {QdTableDataResolver, QdTableDataResolverProps, QdTableResolvedData} from '@quadrel-enterprise-ui/framework';
import {tableColumns} from "../../person/persons-overview/persons-overview.component";
import {PersonService} from "./person.service";

@Injectable()
export class PersonTableDataResolverService implements QdTableDataResolver<tableColumns> {

    constructor(private readonly personService: PersonService) {
    }

    resolve(props: QdTableDataResolverProps<tableColumns>): Observable<QdTableResolvedData<tableColumns>> {
      return this.personService.getPersons().pipe(
            map((persons: PersonDto[]) => {
                if (persons.length === 0) {
                    return {
                        data: [],
                        size: 0,
                        totalElements: 0,
                        page: 0
                    };
                }

                const tableEntries = persons.map(person => ({
                    firstname: person.firstname,
                    lastname: person.lastname,
                    id: person.id
                }));

               const data: QdTableResolvedData<tableColumns> = {
                    data: tableEntries.slice(props.page! * props.size!, (props.page! + 1) * props.size!),
                    size: props.size!,
                    totalElements: persons.length,
                    page: props.page!,
                };
                return data;
            })
        );
    }

}
