const listContainer = document.querySelector('#service-list');
let servicesRequest = new Request('/service');

fetch(servicesRequest)
  .then(response => response.json())
  .then(serviceList => {
    serviceList.forEach(service => {
      var li = document.createElement("li");
      var deleteButton = document.createElement("button");
      deleteButton.onclick = () => onDeleteButton(service.url);
      deleteButton.textContent = "🗑";

      li.appendChild(document.createTextNode(service.url + ': ' + service.status));
      li.appendChild(deleteButton);

      listContainer.appendChild(li);
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
