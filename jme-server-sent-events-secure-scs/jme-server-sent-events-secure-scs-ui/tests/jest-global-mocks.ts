const mock = () => {
  let storage: {[key: string]: string} = {};
  return {
    getItem: (key: string) => (key in storage ? storage[key] : null),
    setItem: (key: string, value: string) => (storage[key] = value || ''),
    removeItem: (key: string) => delete storage[key],
    clear: () => (storage = {})
  };
};

Object.defineProperty(window, 'localStorage', {value: mock()});
Object.defineProperty(window, 'sessionStorage', {value: mock()});
Object.defineProperty(window, 'scrollIntoView', {value: mock()});
// Quadrel's grid reads gridColumnStart/gridColumnEnd of every column and fails on undefined values
Object.defineProperty(window, 'getComputedStyle', {
  value: () => Object.assign(['-webkit-appearance'], {gridColumnStart: 'auto', gridColumnEnd: 'auto'})
});
(window as any).HTMLElement.prototype.scrollIntoView = function () {};
