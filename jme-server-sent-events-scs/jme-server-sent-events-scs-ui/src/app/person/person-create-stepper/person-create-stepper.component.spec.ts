import {TestBed} from '@angular/core/testing';
import {PersonCreateStepperComponent} from './person-create-stepper.component';
import {PersonService} from '../../shared/services/person.service';
import {provideRouter} from '@angular/router';
import {of, throwError} from 'rxjs';
import {StoreModule} from '@ngrx/store';
import {TranslateModule} from '@ngx-translate/core';

describe('PersonCreateStepperComponent', () => {
  let component: PersonCreateStepperComponent;
  let personServiceMock: any;
  let routerMock: any;

  beforeEach(async () => {
    personServiceMock = {
      createPerson: jest.fn()
    };

    routerMock = {
      navigate: jest.fn()
    };

    await TestBed.configureTestingModule({
      imports: [PersonCreateStepperComponent, TranslateModule.forRoot(), StoreModule.forRoot()],
      declarations: [],
      providers: [provideRouter([]), {provide: PersonService, useValue: personServiceMock}]
    }).compileComponents();

    const fixture = TestBed.createComponent(PersonCreateStepperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form controls with empty values', () => {
    expect(component.personalInfos.get('firstName')?.value).toBe('');
    expect(component.personalInfos.get('lastName')?.value).toBe('');
  });

  it('should validate form controls correctly', () => {
    const firstNameControl = component.personalInfos.get('firstName');
    const lastNameControl = component.personalInfos.get('lastName');

    firstNameControl?.setValue('');
    lastNameControl?.setValue('Jo');

    expect(firstNameControl?.valid).toBeFalsy(); // Required validation
    expect(lastNameControl?.valid).toBeFalsy(); // MinLength(3) validation

    lastNameControl?.setValue('John');
    expect(lastNameControl?.valid).toBeTruthy();
  });

  it('should call personService.createPerson on submit', () => {
    const formValue = {
      personalInfos: {
        firstName: 'John',
        lastName: 'Doe'
      }
    };
    personServiceMock.createPerson.mockReturnValue(of({}));

    component.submitAction(formValue);

    expect(personServiceMock.createPerson).toHaveBeenCalledWith('John', 'Doe');
  });

  it('should handle error when createPerson fails', () => {
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
    const formValue = {
      personalInfos: {
        firstName: 'John',
        lastName: 'Doe'
      }
    };
    const errorResponse = {message: 'Error'};
    personServiceMock.createPerson.mockReturnValue(throwError(() => errorResponse));

    component.submitAction(formValue);

    expect(personServiceMock.createPerson).toHaveBeenCalledWith('John', 'Doe');
    expect(consoleSpy).toHaveBeenCalledWith('Error creating person:', errorResponse);
    consoleSpy.mockRestore();
  });
});
