const tableContainer = document.querySelector('#service-list');
let servicesRequest = new Request('/service');

fetch(servicesRequest)
  .then(response => response.json())
  .then(serviceList => {
    serviceList.forEach(service => {
      var row = document.createElement("tr");
      var name_column = document.createElement("td");
      var url_column = document.createElement("td");
      var created_at_column = document.createElement("td");
      var status_column = document.createElement("td");
      var actions_column = document.createElement("td");

      name_column.textContent = service.name;
      url_column.textContent = service.url;
      status_column.textContent = service.status;
      created_at_column.textContent = service.created_at;

      var deleteButton = document.createElement("button");
      deleteButton.onclick = () => onDeleteButton(service.url);
      deleteButton.textContent = "🗑";
      actions_column.appendChild(deleteButton);

      row.appendChild(name_column);
      row.appendChild(url_column);
      row.appendChild(status_column);
      row.appendChild(created_at_column);
      row.appendChild(actions_column);

      tableContainer.appendChild(row);
    });
});

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
  const url = document.querySelector('#service-url').value;
  const name = document.querySelector('#service-name').value;

  fetch('/service', {
    method: 'post',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
    body: JSON.stringify({ url, name })
  }).then(_ => location.reload());
}

const onDeleteButton = (url) => {
  fetch('/service', {
    method: 'delete',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
    body: JSON.stringify({ url })
  }).then(_ => location.reload());
}
